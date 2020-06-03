package com.stuckinadrawer.cookbook.unittest

import java.time.OffsetDateTime

import cats.effect.IO
import com.stuckinadrawer.cookbook.recipes.Recipe.{NewRecipe, RecipeId, RecipeOverview}
import com.stuckinadrawer.cookbook.recipes.{Recipe, RecipeRepository, RecipeService}
import io.circe._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{HttpRoutes, Status, _}

class RecipeServiceTest extends munit.FunSuite with Http4sTestHelper {
  val now = OffsetDateTime.now()

  def mock(get: Recipe.RecipeId => IO[Option[Recipe.Recipe]] = _ => IO(None),
           getAll: Option[String] => IO[List[Recipe.RecipeOverview]] = _ => IO(List.empty))
    : RecipeRepository.Service =
    new RecipeRepository.Service {
      override def getRecipesOverviews(name: Option[String]): IO[List[Recipe.RecipeOverview]] =
        getAll(name)

      override def getRecipeById(id: Recipe.RecipeId): IO[Option[Recipe.Recipe]] = get(id)

      override def delete(id: Recipe.RecipeId): IO[Int] = IO(0)

      override def create(recipe: NewRecipe): IO[Recipe.Recipe] = ???

      override def update(id: Recipe.RecipeId,
                          patch: Recipe.RecipePatch): IO[Option[Recipe.Recipe]] =
        ???
    }
  val pastaRecipe = Recipe.Recipe(RecipeId(1), "pasta", "desc", List.empty, "cook", now, now)

  val pastaJson: Json =
    Json.obj(
      ("id", Json.fromBigInt(1)),
      ("name", Json.fromString("pasta")),
      ("description", Json.fromString("desc")),
      ("ingredients", Json.fromValues(List())),
      ("instructions", Json.fromString("cook")),
      ("created_at", Json.fromString(now.toString)),
      ("update_at", Json.fromString(now.toString))
    )

  test("get recipe by id") {

    val routes: HttpRoutes[IO] =
      new RecipeService(mock(get = _ => IO(Some(pastaRecipe)))).recipeRoutes
    val response = routes.orNotFound.run(Request(method = GET, uri = uri"/recipe/1"))

    check[Json](response, Status.Ok, Some(pastaJson))

  }

  test("get non existing recipe") {
    val routes: HttpRoutes[IO] = new RecipeService(mock()).recipeRoutes
    val response               = routes.orNotFound.run(Request(method = GET, uri = uri"/recipe/6"))
    check[Json](response, Status.NotFound, None)
  }

  test("get all recipes") {
    val routes: HttpRoutes[IO] = new RecipeService(mock(getAll = _ =>
      IO(List(RecipeOverview(pastaRecipe.id, pastaRecipe.name, pastaRecipe.description))))).recipeRoutes
    val response = routes.orNotFound.run(Request(method = GET, uri = uri"/recipe"))
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
