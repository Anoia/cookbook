package com.stuckinadrawer.cookbook.recipes

import java.time.OffsetDateTime

import cats.effect._
import cats.implicits._
import com.stuckinadrawer.cookbook.recipes.Recipe.{
  Ingredient,
  NewRecipe,
  Recipe,
  RecipeId,
  RecipeOverview,
  RecipePatch
}
import doobie._
import Fragments.whereAndOpt
import cats.data.NonEmptyList
import com.stuckinadrawer.cookbook.foodstuffs.FoodStuff.FoodStuffId
import com.typesafe.scalalogging.LazyLogging
import doobie.free.connection
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.update.Update0
import javatime._

final class DoobieRecipeRepository(xa: HikariTransactor[IO]) extends LazyLogging {
  import DoobieRecipeRepository.SQL

  val recipeRepository: RecipeRepository.Service = new RecipeRepository.Service {

    override def getRecipeById(id: RecipeId): IO[Option[Recipe]] = getRecipe(id).transact(xa)

    override def getRecipesOverviews(name: Option[String]): IO[List[RecipeOverview]] =
      SQL.getRecipesOverviews(name).to[List].transact(xa)

    override def delete(id: RecipeId): IO[Int] = SQL.deleteRecipe(id).run.transact(xa)

    override def create(recipe: NewRecipe): IO[Recipe] = {
      SQL
        .create(recipe)
        .withUniqueGeneratedKeys[IncompleteRecipe](
          "recipe_id",
          "recipe_name",
          "description",
          "instructions",
          "created_at",
          "updated_at"
        )
        .map(x => assembleRecipe(x, List.empty))
        .transact(xa)
    }

    override def update(id: RecipeId, patch: RecipePatch): IO[Option[Recipe]] =
      getAndUpdateRecipeWithPatch(id, patch).transact(xa)

    def getAndUpdateRecipeWithPatch(id: RecipeId,
                                    patch: RecipePatch): ConnectionIO[Option[Recipe]] =
      for {
        old <- getRecipe(id)
        patched = old.map(_.update(patch))
        result <- patched.map(updateRecipe).getOrElse(connection.pure(Option.empty[Recipe]))

      } yield {
        result // read back from db afterwards?  to ensure new update_at timestamp
      }

    def updateRecipe(recipe: Recipe): ConnectionIO[Option[Recipe]] = { // TODO fix this connectionIO/Options stuff
      for {
        _ <- SQL.updateRecipeData(IncompleteRecipe.fromRecipe(recipe)).run
        _ <- removeOldIngredientsFromRecipe(recipe.id, recipe.ingredients)
        _ <- addNewIngredientsToRecipe(recipe.id, recipe.ingredients)
      } yield Some(recipe)
    }

    def getIngredients(recipeId: RecipeId): ConnectionIO[List[Ingredient]] =
      SQL.getIngredientsForRecipe(recipeId).to[List]

    def addNewIngredientsToRecipe(recipeId: RecipeId,
                                  ingredients: List[Ingredient]): doobie.ConnectionIO[Int] = {
      val values = ingredients.map(i => (recipeId, i.foodStuffId, i.amount))
      SQL.insertOrUpdateRecipeIngredients().updateMany(values)
    }

    def removeOldIngredientsFromRecipe(recipeId: RecipeId,
                                       ingredients: List[Ingredient]): ConnectionIO[Int] =
      SQL.removeOldIngredientsFromRecipe(recipeId, ingredients.map(_.foodStuffId)).run

    def getIncompleteRecipeData(recipeId: RecipeId): ConnectionIO[Option[IncompleteRecipe]] =
      SQL.getRecipeDataForRecipe(recipeId).option

    def getRecipe(recipeId: RecipeId): ConnectionIO[Option[Recipe]] = {
      for {
        recipeData  <- getIncompleteRecipeData(recipeId)
        ingredients <- getIngredients(recipeId)
      } yield {
        logger.info(s"read recipe data from db! $recipeData, $ingredients")
        recipeData.map(assembleRecipe(_, ingredients))
      }
    }

    def assembleRecipe(recipeData: IncompleteRecipe, ingredients: List[Ingredient]): Recipe = {
      Recipe(recipeData.id,
             recipeData.name,
             recipeData.description,
             ingredients,
             recipeData.instructions,
             recipeData.created_at,
             recipeData.update_at)
    }
  }
}

case class IncompleteRecipe(id: RecipeId,
                            name: String,
                            description: String,
                            instructions: String,
                            created_at: OffsetDateTime,
                            update_at: OffsetDateTime) {
  def update(patch: RecipePatch): IncompleteRecipe = {
    copy(
      name = patch.name.getOrElse(name),
      description = patch.description.getOrElse(description),
      instructions = patch.instructions.getOrElse(instructions)
    )
  }
}

object IncompleteRecipe {
  def fromRecipe(recipe: Recipe): IncompleteRecipe =
    IncompleteRecipe(recipe.id,
                     recipe.name,
                     recipe.description,
                     recipe.instructions,
                     recipe.created_at,
                     recipe.update_at)
}

object DoobieRecipeRepository {

  type RecipeIngredient = (RecipeId, FoodStuffId, Int)

  object SQL {

    def create(recipe: NewRecipe): Update0 =
      sql"""
         INSERT INTO recipe(recipe_name)
         VALUES (${recipe.name})
         """.update

    def getRecipesOverviews(name: Option[String]): Query0[RecipeOverview] = {
      val f1 = name.map(n => fr"recipe_name ILIKE '%' || $n || '%'")
      val q: Fragment =
        fr"SELECT recipe_id, recipe_name, description FROM recipe" ++
          whereAndOpt(f1) ++
          fr"ORDER BY created_at DESC"
      q.query[RecipeOverview]
    }

    def updateRecipeData(recipe: IncompleteRecipe): Update0 =
      sql"""
         UPDATE recipe SET
         recipe_name = ${recipe.name},
         description = ${recipe.description},
         instructions = ${recipe.instructions}
         WHERE recipe_id= ${recipe.id.value}
         """.update

    def deleteRecipe(id: RecipeId): Update0 =
      sql"""DELETE FROM recipe WHERE recipe_id=${id.value}""".update

    def getIngredientsForRecipe(id: RecipeId): doobie.Query0[Ingredient] =
      sql"""SELECT foodstuff.foodstuff_id, foodstuff_name, amount FROM foodstuff
            INNER JOIN recipe_ingredients on recipe_ingredients.foodstuff_id = foodstuff.foodstuff_id
           WHERE recipe_id  = ${id.value}""".query[Ingredient]

    def insertOrUpdateRecipeIngredients(): Update[RecipeIngredient] = Update[RecipeIngredient] {
      """
           INSERT INTO recipe_ingredients (recipe_id, foodstuff_id, amount)
              values (?, ?, ?)
           ON CONFLICT ON CONSTRAINT recipe_ingredients_pkey
           DO
             UPDATE SET amount = excluded.amount"""
    }

    def removeOldIngredientsFromRecipe(recipeId: RecipeId,
                                       ingredientsToKeep: List[FoodStuffId]): doobie.Update0 = {
      val ingredientFragment =
        NonEmptyList
          .fromList(ingredientsToKeep)
          .map(i => Fragments.notIn(fr"foodstuff_id", i))
          .map(fr" AND " ++ _)
          .getOrElse(fr"")

      (fr"""
             DELETE FROM recipe_ingredients
             WHERE recipe_id=${recipeId.value}
             """ ++ ingredientFragment).update
    }

    def getRecipeDataForRecipe(id: RecipeId): Query0[IncompleteRecipe] =
      sql"""
         SELECT recipe_id, recipe_name, description, instructions, created_at, updated_at
         FROM recipe where recipe_id = ${id.value}
          """.query[IncompleteRecipe]
  }
}
