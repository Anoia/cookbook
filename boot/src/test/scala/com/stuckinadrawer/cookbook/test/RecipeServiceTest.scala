package com.stuckinadrawer.cookbook.test

import java.time.OffsetDateTime

import cats.effect.IO
import com.stuckinadrawer.cookbook.recipes.Recipe.NewRecipe
import com.stuckinadrawer.cookbook.recipes.RecipeService
import io.circe._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{HttpRoutes, Status, _}

class RecipeServiceTest extends munit.FunSuite with Http4sTestHelper {
  val now                    = OffsetDateTime.now()
  val inMemoryRepo           = new InMemoryRecipeRepo(now)
  val routes: HttpRoutes[IO] = new RecipeService(inMemoryRepo).recipeRoutes

  val pastaRecipe: NewRecipe = NewRecipe("pasta", "desc", List("pasta", "tomatoes"), "cook")

  val pastaJson: Json =
    Json.obj(
      ("id", Json.fromBigInt(1)),
      ("name", Json.fromString("pasta")),
      ("description", Json.fromString("desc")),
      ("ingredients", Json.fromValues(List(Json.fromString("pasta"), Json.fromString("tomatoes")))),
      ("instructions", Json.fromString("cook")),
      ("created_at", Json.fromString(now.toString)),
      ("update_at", Json.fromString(now.toString))
    )

  test("get recipe by id") {
    inMemoryRepo.create(pastaRecipe).unsafeRunSync()

    val response = routes.orNotFound.run(Request(method = GET, uri = uri"/recipes/1"))

    check[Json](response, Status.Ok, Some(pastaJson))

  }

  test("get non existing recipe") {
    val response = routes.orNotFound.run(Request(method = GET, uri = uri"/recipes/6"))
    check[Json](response, Status.NotFound, None)
  }

  test("get all recipes") {
    val response = routes.orNotFound.run(Request(method = GET, uri = uri"/recipes"))
    val expectedJson = Json.fromValues(
      List(
        Json.obj(("id", Json.fromBigInt(1)),
                 ("name", Json.fromString("pasta")),
                 ("description", Json.fromString("desc")))
      )
    )
    check[Json](response, Status.Ok, Some(expectedJson))
  }
}
