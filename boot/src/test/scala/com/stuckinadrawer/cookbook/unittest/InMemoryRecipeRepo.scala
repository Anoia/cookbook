package com.stuckinadrawer.cookbook.unittest

import java.time.OffsetDateTime

import cats.effect.IO
import com.stuckinadrawer.cookbook.recipes.Recipe._
import com.stuckinadrawer.cookbook.recipes.RecipeRepository

class InMemoryRecipeRepo(now: OffsetDateTime) extends RecipeRepository.Service {

  var repo: Map[RecipeId, Recipe] = Map.empty
  var id: Int                     = 0

  override def getAll(name: Option[String] = None): IO[List[RecipeOverview]] = IO {
    repo.values
      .map(r => RecipeOverview(r.id, r.name, r.description))
      .toList
      .filter(r => name.forall(_ == r.name))
  }

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
             recipe.name,
             recipe.description,
             recipe.ingredients,
             recipe.instructions,
             now,
             now)
    repo = repo + (r.id -> r)
    r
  }

  override def update(id: RecipeId, patch: RecipePatch): IO[Option[Recipe]] =
    IO {
      repo.get(id).map(_.update(patch))
    }
}
