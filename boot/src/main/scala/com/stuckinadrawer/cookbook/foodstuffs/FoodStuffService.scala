package com.stuckinadrawer.cookbook.foodstuffs

import cats.effect.IO
import com.stuckinadrawer.cookbook.foodstuffs.FoodStuff.{FoodStuffId, FoodStuffPatch, NewFoodStuff}
import com.stuckinadrawer.cookbook.util.ServiceUtils
import com.stuckinadrawer.cookbook.util.ServiceUtils.OptionalNameQueryParamMatcher
import io.circe.{Encoder, Json}
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.server.Router

import scala.util.{Success, Try}

class FoodStuffService(repo: FoodStuffRepository.Service) {

  object FoodStuffIdVar {
    def unapply(arg: String): Option[FoodStuffId] = {
      Try(arg.toLong) match {
        case Success(value) if value > 0 => Some(FoodStuffId(value))
        case _                           => None
      }
    }
  }
  implicit val encodeFoodStuffId: Encoder[FoodStuffId] = (a: FoodStuffId) => Json.fromLong(a.value)

  private val http: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root :? OptionalNameQueryParamMatcher(name) =>
      repo.getAll(name).flatMap(Ok(_))

    case GET -> Root / FoodStuffIdVar(foodStuffId) =>
      repo.getById(foodStuffId).flatMap {
        case Some(value) => Ok(value)
        case None        => NotFound()
      }

    case req @ POST -> Root =>
      for {
        newRecipe <- req.as[NewFoodStuff]
        created   <- repo.create(newRecipe)
        response  <- Created(created)
      } yield response

    case req @ PATCH -> Root / FoodStuffIdVar(recipeId) =>
      for {
        patch    <- req.as[FoodStuffPatch]
        patched  <- repo.update(recipeId, patch)
        response <- ServiceUtils.optionToResponse(patched)
      } yield {
        response
      }

    case DELETE -> Root / FoodStuffIdVar(foodStuffId) =>
      repo.delete(foodStuffId) flatMap {
        case 0 => NotFound()
        case 1 => NoContent()
        case _ => InternalServerError()
      }
  }

  val foodStuffRoutes: HttpRoutes[IO] = {
    Router("/foodstuff" -> http)
  }
}
