package com.stuckinadrawer.cookbook.domain

import java.time.OffsetDateTime

object Recipe {

  final case class RecipeId(value: Long) extends AnyVal

  type Ingredient = String

  final case class Recipe(id: RecipeId, recipeData: RecipeData) {
    def update(patch: RecipePatch): Recipe = {
      copy(
        recipeData = recipeData.copy(
          name = patch.name.getOrElse(recipeData.name),
          description = patch.description.getOrElse(recipeData.description),
          ingredients = patch.ingredients.getOrElse(recipeData.ingredients),
          instructions = patch.instructions.getOrElse(recipeData.instructions)
        )
      )
    }
  }

  final case class RecipeOverview(name: String, description: String)

  final case class RecipeData(name: String,
                              description: String,
                              ingredients: List[Ingredient],
                              instructions: String,
                              created_at: OffsetDateTime,
                              update_at: OffsetDateTime)

  final case class NewRecipe(name: String,
                             description: String,
                             ingredients: List[Ingredient],
                             instructions: String)

  final case class RecipePatch(name: Option[String] = None,
                               description: Option[String] = None,
                               ingredients: Option[List[Ingredient]] = None,
                               instructions: Option[String] = None)
}
