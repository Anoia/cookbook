package com.stuckinadrawer.cookbook.service

import cats.effect._
import com.stuckinadrawer.cookbook.domain.CookBook.RecipeId
import com.stuckinadrawer.cookbook.storage.RecipeRepository
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.io._

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

  val http: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello $name!")
    case GET -> Root / "recipes" =>
      repo.getAll.flatMap(Ok(_))
    case GET -> Root / "recipes" / RecipeIdVar(recipeId) =>
      repo.getById(recipeId).flatMap {
        case Some(value) => Ok(value)
        case None        => NotFound()
      }
  }

}
