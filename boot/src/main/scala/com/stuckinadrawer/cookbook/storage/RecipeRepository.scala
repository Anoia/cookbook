package com.stuckinadrawer.cookbook.storage

import cats.effect.IO
import com.stuckinadrawer.cookbook.domain.CookBook.{NewRecipe, Recipe, RecipeId, RecipePatch}

object RecipeRepository {
  trait Service {
    def getAll: IO[List[Recipe]]
    def getById(id: RecipeId): IO[Option[Recipe]]
    def delete(id: RecipeId): IO[Int]
    def create(recipe: NewRecipe): IO[Recipe]
    def update(id: RecipeId, patch: RecipePatch): IO[Option[Recipe]]
  }
}
