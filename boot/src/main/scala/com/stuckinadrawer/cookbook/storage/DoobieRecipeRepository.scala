package com.stuckinadrawer.cookbook.storage

import cats.effect._
import cats.implicits._
import com.stuckinadrawer.cookbook.domain.PostgresConfig
import com.stuckinadrawer.cookbook.domain.Recipe.{NewRecipe, Recipe, RecipeId, RecipePatch}
import doobie._
import doobie.free.connection
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.update.Update0
import doobie.postgres.implicits._
import doobie.util.meta.TimeMeta
import javatime._

final class DoobieRecipeRepository(xa: HikariTransactor[IO]) {
  import DoobieRecipeRepository.SQL

  val recipeRepository: RecipeRepository.Service = new RecipeRepository.Service {

    override def getById(id: RecipeId): IO[Option[Recipe]] =
      SQL.get(id).option.transact(xa)

    override def getAll: IO[List[Recipe]] = SQL.getAll.to[List].transact(xa)

    override def delete(id: RecipeId): IO[Int] = SQL.delete(id).run.transact(xa)

    override def create(recipe: NewRecipe): IO[Recipe] = {
      SQL
        .create(recipe)
        .withUniqueGeneratedKeys[Recipe](
          "id",
          "name",
          "description",
          "ingredients",
          "instructions",
          "created_at",
          "updated_at"
        )
        .transact(xa)
    }

    override def update(id: RecipeId, patch: RecipePatch): IO[Option[Recipe]] =
      (for {
        old <- SQL.get(id).option
        patched = old.map(_.update(patch))
        _ <- patched.fold(connection.unit)(r => SQL.update(r).run.void)
      } yield {
        patched
      }).transact(xa)
  }
}

object DoobieRecipeRepository {

  import doobie.hikari._

  def createRecipeRepository(
      cfg: PostgresConfig
  )(implicit cs: ContextShift[IO]): Resource[IO, RecipeRepository.Service] = {
    // implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32)
      be <- Blocker[IO]
      xa <- HikariTransactor
        .newHikariTransactor[IO](
          cfg.driver,
          cfg.url,
          cfg.user,
          cfg.pass,
          ce,
          be
        )
    } yield new DoobieRecipeRepository(xa).recipeRepository
  }

  object SQL {

    def create(recipe: NewRecipe): Update0 =
      sql"""
         INSERT INTO recipe(name, description, ingredients, instructions) 
         VALUES (${recipe.name}, ${recipe.description}, ${recipe.ingredients}, ${recipe.instructions})
         """.update

    def get(id: RecipeId): Query0[Recipe] =
      sql"""
         SELECT id, name, description, ingredients, instructions, created_at, updated_at
         FROM recipe where id = ${id.value}
         """.query[Recipe]

    def getAll: Query0[Recipe] =
      sql"""SELECT id, name, description, ingredients, instructions, created_at, updated_at FROM recipe"""
        .query[Recipe]

    def update(recipe: Recipe): Update0 =
      sql"""
         UPDATE recipe SET
         name = ${recipe.recipeData.name},
         description = ${recipe.recipeData.description},
         ingredients = ${recipe.recipeData.ingredients},
         instructions = ${recipe.recipeData.instructions}
         WHERE id= ${recipe.id.value}
         """.update

    def delete(id: RecipeId): Update0 =
      sql"""DELETE FROM recipe WHERE id=${id.value}""".update
  }
}
