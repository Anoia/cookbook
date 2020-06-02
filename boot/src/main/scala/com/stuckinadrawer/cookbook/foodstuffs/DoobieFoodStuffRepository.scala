package com.stuckinadrawer.cookbook.foodstuffs

import cats.effect._
import cats.implicits._
import com.stuckinadrawer.cookbook.foodstuffs.FoodStuff.{
  FoodStuff,
  FoodStuffId,
  FoodStuffOverview,
  FoodStuffPatch,
  NewFoodStuff
}
import doobie.free.connection
import doobie.hikari.HikariTransactor
import doobie.{Fragment, Query0}
import doobie.util.update.Update0
import doobie.implicits._
import doobie.util.fragments.whereAndOpt
import javatime._

final class DoobieFoodStuffRepository(xa: HikariTransactor[IO]) {
  import DoobieFoodStuffRepository.SQL

  val foodStuffRepository: FoodStuffRepository.Service = new FoodStuffRepository.Service {

    override def getAll(name: Option[String]): IO[List[FoodStuffOverview]] =
      SQL.getAll(name).to[List].transact(xa)

    override def getById(id: FoodStuffId): IO[Option[FoodStuff]] = SQL.get(id).option.transact(xa)

    override def delete(id: FoodStuffId): IO[Int] = SQL.delete(id).run.transact(xa)

    override def create(foodStuff: NewFoodStuff): IO[FoodStuff] = {
      SQL
        .create(foodStuff)
        .withUniqueGeneratedKeys[FoodStuff](
          "foodstuff_id",
          "foodstuff_name",
          "description",
          "created_at",
          "updated_at"
        )
        .transact(xa)
    }

    override def update(id: FoodStuffId, patch: FoodStuffPatch): IO[Option[FoodStuff]] =
      (for {
        old <- SQL.get(id).option
        patched = old.map(_.update(patch))
        _ <- patched.fold(connection.unit)(r => SQL.update(r).run.void)
      } yield {
        patched
      }).transact(xa)
  }
}

object DoobieFoodStuffRepository {

  object SQL {

    def create(foodStuff: NewFoodStuff): Update0 =
      sql"""
         INSERT INTO foodstuff(foodstuff_name) 
         VALUES (${foodStuff.name})
         """.update

    def get(id: FoodStuffId): Query0[FoodStuff] =
      sql"""
         SELECT foodstuff_id, foodstuff_name, description, created_at, updated_at
         FROM foodstuff where foodstuff_id = ${id.value}
         """.query[FoodStuff]

    def getAll(name: Option[String]): Query0[FoodStuffOverview] = {
      val f1 = name.map(n => fr"foodstuff_name ILIKE '%' || $n || '%'")
      val q: Fragment =
        fr"SELECT foodstuff_id, foodstuff_name FROM foodstuff" ++
          whereAndOpt(f1) ++
          fr"ORDER BY created_at DESC"
      q.query[FoodStuffOverview]
    }

    def update(foodStuff: FoodStuff): Update0 =
      sql"""
         UPDATE foodstuff SET
         foodstuff_name = ${foodStuff.name},
         description = ${foodStuff.description}
         WHERE foodstuff_id= ${foodStuff.id.value}
         """.update

    def delete(id: FoodStuffId): Update0 =
      sql"""DELETE FROM foodstuff WHERE foodstuff_id=${id.value}""".update
  }

}
