package com.stuckinadrawer.cookbook.storage

import cats.effect.IO
import com.stuckinadrawer.cookbook.domain.Recipe.{
  NewRecipe,
  Recipe,
  RecipeId,
  RecipeOverview,
  RecipePatch
}

object RecipeRepository {
  trait Service {
    def getAll: IO[List[RecipeOverview]]
    def getById(id: RecipeId): IO[Option[Recipe]]
    def delete(id: RecipeId): IO[Int]
    def create(recipe: NewRecipe): IO[Recipe]
    def update(id: RecipeId, patch: RecipePatch): IO[Option[Recipe]]
  }
}
