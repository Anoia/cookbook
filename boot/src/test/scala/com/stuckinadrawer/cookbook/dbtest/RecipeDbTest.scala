package com.stuckinadrawer.cookbook.dbtest

import com.stuckinadrawer.cookbook.foodstuffs.FoodStuff.FoodStuffId
import com.stuckinadrawer.cookbook.recipes.DoobieRecipeRepository.SQL
import com.stuckinadrawer.cookbook.recipes.Recipe.RecipeId

class RecipeDbTest extends DbTest {

  check(SQL.getRecipesOverviews(Some("")))
  check(SQL.deleteRecipe(RecipeId(1)))

  // check(SQL.getRecipeDataForRecipe(RecipeId(1)))  // TODO timestamp issue
  check(SQL.getIngredientsForRecipe(RecipeId(1)))

  check(SQL.removeOldIngredientsFromRecipe(RecipeId(1), List.empty))
  check(SQL.removeOldIngredientsFromRecipe(RecipeId(1), List(FoodStuffId(1))))

  check(SQL.insertOrUpdateRecipeIngredients())

}
