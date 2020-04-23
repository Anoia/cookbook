package com.stuckinadrawer.bookbook.domain

object CookBook {

  final case class RecipeId(value: Long) extends AnyVal

  type Ingredient = String

  final case class Recipe(id: RecipeId, recipeData: RecipeData) {
    def update(patch: RecipePatch): Recipe = {
      copy(
        recipeData = recipeData.copy(
          name = patch.name.getOrElse(recipeData.name),
          ingredients = patch.ingredients.getOrElse(recipeData.ingredients),
          instructions = patch.instructions.getOrElse(recipeData.instructions)
        )
      )
    }
  }
  final case class RecipeData(name: String, ingredients: List[Ingredient], instructions: String)

  final case class NewRecipe(name: String, ingredients: List[Ingredient], instructions: String)

  final case class RecipePatch(name: Option[String] = None,
                               ingredients: Option[List[Ingredient]] = None,
                               instructions: Option[String] = None)
}
