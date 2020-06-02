package com.stuckinadrawer.cookbook.recipes

import cats.effect.IO
import Recipe.{NewRecipe, RecipeId, RecipePatch}
import com.stuckinadrawer.cookbook.util.ServiceUtils
import com.stuckinadrawer.cookbook.util.ServiceUtils.OptionalNameQueryParamMatcher
import io.circe.generic.auto._
import io.circe.{Encoder, Json}
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.io._
import org.http4s.server.Router

import scala.util.{Success, Try}

class RecipeService(repo: RecipeRepository.Service) {

  object RecipeIdVar {
    def unapply(arg: String): Option[RecipeId] = {
      Try(arg.toInt) match {
        case Success(value) if value > 0 => Some(RecipeId(value))
        case _                           => None
      }
    }
  }

  implicit val encodeRecipeId: Encoder[RecipeId] = (a: RecipeId) => Json.fromInt(a.value)

  private val http: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root :? OptionalNameQueryParamMatcher(name) =>
      repo.getAll(name).flatMap(Ok(_))

    case GET -> Root / RecipeIdVar(recipeId) =>
      repo.getById(recipeId).flatMap {
        case Some(value) => Ok(value)
        case None        => NotFound()
      }

    case req @ POST -> Root =>
      for {
        newRecipe <- req.as[NewRecipe]
        created   <- repo.create(newRecipe)
        response  <- Created(created)
      } yield response

    case req @ PATCH -> Root / RecipeIdVar(recipeId) =>
      for {
        patch    <- req.as[RecipePatch]
        patched  <- repo.update(recipeId, patch)
        response <- ServiceUtils.optionToResponse(patched)
      } yield {
        response
      }

    case DELETE -> Root / RecipeIdVar(recipeId) =>
      repo.delete(recipeId) flatMap {
        case 0 => NotFound()
        case 1 => NoContent()
        case _ => InternalServerError()
      }

  }

  val recipeRoutes: HttpRoutes[IO] = {
    Router("/recipe" -> http)
  }

}
