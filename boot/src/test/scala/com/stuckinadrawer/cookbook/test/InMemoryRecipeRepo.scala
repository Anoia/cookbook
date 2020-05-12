package com.stuckinadrawer.cookbook.test

import cats.effect.IO
import com.stuckinadrawer.cookbook.domain.CookBook
import com.stuckinadrawer.cookbook.domain.CookBook.{Recipe, RecipeData, RecipeId}
import com.stuckinadrawer.cookbook.storage.RecipeRepository

class InMemoryRecipeRepo extends RecipeRepository.Service {

  var repo: Map[RecipeId, Recipe] = Map.empty
  var id: Long                    = 0

  override def getAll: IO[List[CookBook.Recipe]] = IO { repo.values.toList }

  override def getById(id: CookBook.RecipeId): IO[Option[CookBook.Recipe]] = IO { repo.get(id) }

  override def delete(id: CookBook.RecipeId): IO[Int] = IO {
    val oldCount = repo.size
    repo = repo.view.filterKeys(_ != id).toMap
    oldCount - repo.size
  }

  override def create(recipe: CookBook.NewRecipe): IO[CookBook.Recipe] = IO {
    id += 1
    val r = Recipe(RecipeId(id), RecipeData(recipe.name, recipe.ingredients, recipe.instructions))
    repo = repo + (r.id -> r)
    r
  }

  override def update(id: CookBook.RecipeId,
                      patch: CookBook.RecipePatch): IO[Option[CookBook.Recipe]] = IO {
    repo.get(id).map(_.update(patch))
  }
}
