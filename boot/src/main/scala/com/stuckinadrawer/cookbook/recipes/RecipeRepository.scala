package com.stuckinadrawer.cookbook.recipes

import cats.effect.IO
import com.stuckinadrawer.cookbook.recipes.Recipe.{
  NewRecipe,
  Recipe,
  RecipeId,
  RecipeOverview,
  RecipePatch
}

object RecipeRepository {
  trait Service {
    def getRecipesOverviews(name: Option[String]): IO[List[RecipeOverview]]
    def getRecipeById(id: RecipeId): IO[Option[Recipe]]
    def delete(id: RecipeId): IO[Int]
    def create(recipe: NewRecipe): IO[Recipe]
    def update(id: RecipeId, patch: RecipePatch): IO[Option[Recipe]]
  }
}
