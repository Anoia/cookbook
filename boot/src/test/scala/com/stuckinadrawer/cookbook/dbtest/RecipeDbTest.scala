package com.stuckinadrawer.cookbook.dbtest

import java.time.OffsetDateTime

import com.stuckinadrawer.cookbook.recipes.DoobieRecipeRepository.SQL
import com.stuckinadrawer.cookbook.recipes.Recipe.{NewRecipe, Recipe, RecipeId}

class RecipeDbTest extends DbTest {

  check(SQL.create(NewRecipe("", "", List.empty, "")))
  // checkOutput(SQL.get(RecipeId(1)))
  check(SQL.getAll(Some("")))
  check(
    SQL.update(
      Recipe(RecipeId(1), "", "", List.empty, "", OffsetDateTime.now(), OffsetDateTime.now())))
  check(SQL.delete(RecipeId(1)))

}
