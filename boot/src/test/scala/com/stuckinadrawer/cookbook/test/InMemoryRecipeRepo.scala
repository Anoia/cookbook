package com.stuckinadrawer.cookbook.test

import java.time.OffsetDateTime

import cats.effect.IO
import com.stuckinadrawer.cookbook.domain.Recipe._
import com.stuckinadrawer.cookbook.storage.RecipeRepository

class InMemoryRecipeRepo extends RecipeRepository.Service {

  var repo: Map[RecipeId, Recipe] = Map.empty
  var id: Long                    = 0

  override def getAll: IO[List[Recipe]] = IO { repo.values.toList }

  override def getById(id: RecipeId): IO[Option[Recipe]] = IO { repo.get(id) }

  override def delete(id: RecipeId): IO[Int] = IO {
    val oldCount = repo.size
    repo = repo.view.filterKeys(_ != id).toMap
    oldCount - repo.size
  }

  override def create(recipe: NewRecipe): IO[Recipe] = IO {
    id += 1
    val r =
      Recipe(RecipeId(id),
             RecipeData(recipe.name,
                        recipe.description,
                        recipe.ingredients,
                        recipe.instructions,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()))
    repo = repo + (r.id -> r)
    r
  }

  override def update(id: RecipeId, patch: RecipePatch): IO[Option[Recipe]] =
    IO {
      repo.get(id).map(_.update(patch))
    }
}
