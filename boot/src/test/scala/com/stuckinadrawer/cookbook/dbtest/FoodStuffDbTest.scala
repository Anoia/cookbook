package com.stuckinadrawer.cookbook.dbtest

import java.time.OffsetDateTime

import com.stuckinadrawer.cookbook.foodstuffs.DoobieFoodStuffRepository.SQL
import com.stuckinadrawer.cookbook.foodstuffs.FoodStuff.{FoodStuff, FoodStuffId, NewFoodStuff}

class FoodStuffDbTest extends DbTest {

  check(SQL.create(NewFoodStuff("")))
  //check(SQL.get(FoodStuffId(1)))
  check(SQL.getAll(Some("")))
  check(
    SQL.update(FoodStuff(FoodStuffId(1), "", "None", OffsetDateTime.now(), OffsetDateTime.now())))
  check(SQL.delete(FoodStuffId(1)))

}
