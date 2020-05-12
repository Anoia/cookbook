package com.stuckinadrawer.cookbook.service

import cats.effect.IO
import com.stuckinadrawer.cookbook.domain.CookBook.{NewRecipe, RecipeId, RecipePatch}
import com.stuckinadrawer.cookbook.storage.RecipeRepository
import io.circe.{Encoder, Json}
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.io._
import org.http4s.server.Router

import scala.util.{Success, Try}

class RecipeService(repo: RecipeRepository.Service) {

  object RecipeIdVar {
    def unapply(arg: String): Option[RecipeId] = {
      Try(arg.toLong) match {
        case Success(value) if value > 0 => Some(RecipeId(value))
        case _                           => None
      }
    }
  }

  implicit val encodeRecipeId: Encoder[RecipeId] = (a: RecipeId) => Json.fromLong(a.value)

  def optionToResponse[A](
      o: Option[A]
  )(implicit entityEncoder: EntityEncoder[IO, A]): IO[Response[IO]] = o match {
    case Some(value) => Ok(value)
    case None        => NotFound()
  }

  private val http: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root =>
      repo.getAll.flatMap(Ok(_))

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
        response <- optionToResponse(patched)
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
    Router("/recipes" -> http)
  }

}
