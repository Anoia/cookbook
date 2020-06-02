package com.stuckinadrawer.cookbook.foodstuffs

import java.time.OffsetDateTime

object FoodStuff {

  final case class FoodStuffId(value: Int) extends AnyVal

  case class FoodStuff(id: FoodStuffId,
                       name: String,
                       description: String,
                       created_at: OffsetDateTime,
                       update_at: OffsetDateTime) {
    def update(patch: FoodStuffPatch): FoodStuff =
      copy(name = patch.name.getOrElse(name),
           description = patch.description.getOrElse(description))
  }

  case class FoodStuffOverview(id: FoodStuffId, name: String)

  case class NewFoodStuff(name: String)

  case class FoodStuffPatch(name: Option[String] = None, description: Option[String] = None)
}
