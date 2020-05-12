package com.stuckinadrawer.cookbook.test

import cats.effect.IO
import com.stuckinadrawer.cookbook.domain.CookBook.NewRecipe
import com.stuckinadrawer.cookbook.service.RecipeService
import io.circe._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{HttpRoutes, Status, _}

class RecipeServiceTest extends munit.FunSuite with Http4sTestHelper {

  val inMemoryRepo           = new InMemoryRecipeRepo()
  val routes: HttpRoutes[IO] = new RecipeService(inMemoryRepo).recipeRoutes

  val pastaRecipe: NewRecipe = NewRecipe("pasta", List("pasta", "tomatoes"), "cook")

  val pastaJson: Json =
    Json.obj(
      ("id", Json.fromBigInt(1)),
      ("recipeData",
       Json.obj(
         ("name", Json.fromString("pasta")),
         ("ingredients",
          Json.fromValues(List(Json.fromString("pasta"), Json.fromString("tomatoes")))),
         ("instructions", Json.fromString("cook"))
       ))
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
      List(pastaJson)
    )
    check[Json](response, Status.Ok, Some(expectedJson))
  }
}
