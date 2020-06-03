package com.stuckinadrawer.cookbook.recipes

import java.time.OffsetDateTime

import com.stuckinadrawer.cookbook.foodstuffs.FoodStuff.FoodStuffId

object Recipe {

  final case class RecipeId(value: Int) extends AnyVal

  final case class Ingredient(foodStuffId: FoodStuffId, name: String, amount: Int)

  final case class Recipe(id: RecipeId,
                          name: String,
                          description: String,
                          ingredients: List[Ingredient],
                          instructions: String,
                          created_at: OffsetDateTime,
                          update_at: OffsetDateTime) {
    def update(patch: RecipePatch): Recipe = {
      copy(
        name = patch.name.getOrElse(name),
        description = patch.description.getOrElse(description),
        ingredients = patch.ingredients.getOrElse(ingredients),
        instructions = patch.instructions.getOrElse(instructions)
      )
    }
  }

  final case class RecipeOverview(id: RecipeId, name: String, description: String)

  final case class NewRecipe(name: String)

  final case class RecipePatch(name: Option[String] = None,
                               description: Option[String] = None,
                               ingredients: Option[List[Ingredient]] = None,
                               instructions: Option[String] = None)
}
