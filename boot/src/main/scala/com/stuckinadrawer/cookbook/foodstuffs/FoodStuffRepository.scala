package com.stuckinadrawer.cookbook.foodstuffs

import cats.effect.IO
import com.stuckinadrawer.cookbook.foodstuffs.FoodStuff.{
  FoodStuff,
  FoodStuffId,
  FoodStuffOverview,
  FoodStuffPatch,
  NewFoodStuff
}

object FoodStuffRepository {
  trait Service {
    def getAll(name: Option[String]): IO[List[FoodStuffOverview]]
    def getById(id: FoodStuffId): IO[Option[FoodStuff]]
    def delete(id: FoodStuffId): IO[Int]
    def create(foodStuff: NewFoodStuff): IO[FoodStuff]
    def update(id: FoodStuffId, patch: FoodStuffPatch): IO[Option[FoodStuff]]
  }
}
