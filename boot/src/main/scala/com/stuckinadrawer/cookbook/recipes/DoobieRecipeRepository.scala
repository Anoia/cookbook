package com.stuckinadrawer.cookbook.recipes

import cats.effect._
import cats.implicits._
import com.stuckinadrawer.cookbook.recipes.Recipe.{
  NewRecipe,
  Recipe,
  RecipeId,
  RecipeOverview,
  RecipePatch
}
import doobie._
import Fragments.whereAndOpt
import com.stuckinadrawer.cookbook.boot.PostgresConfig
import doobie.free.connection
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.update.Update0
import javatime._

final class DoobieRecipeRepository(xa: HikariTransactor[IO]) {
  import DoobieRecipeRepository.SQL

  val recipeRepository: RecipeRepository.Service = new RecipeRepository.Service {

    override def getById(id: RecipeId): IO[Option[Recipe]] =
      SQL.get(id).option.transact(xa)

    override def getAll(name: Option[String]): IO[List[RecipeOverview]] =
      SQL.getAll(name).to[List].transact(xa)

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

    def getAll(name: Option[String]): Query0[RecipeOverview] = {
      val f1 = name.map(n => fr"name ILIKE '%' || $n || '%'")
      val q: Fragment =
        fr"SELECT id, name, description FROM recipe" ++
          whereAndOpt(f1) ++
          fr"ORDER BY created_at DESC"
      q.query[RecipeOverview]
    }

    def update(recipe: Recipe): Update0 =
      sql"""
         UPDATE recipe SET
         name = ${recipe.name},
         description = ${recipe.description},
         ingredients = ${recipe.ingredients},
         instructions = ${recipe.instructions}
         WHERE id= ${recipe.id.value}
         """.update

    def delete(id: RecipeId): Update0 =
      sql"""DELETE FROM recipe WHERE id=${id.value}""".update
  }
}
