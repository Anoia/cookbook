package com.stuckinadrawer.cookbook.boot
import doobie._
import doobie.implicits._
import cats.effect.IO
import com.stuckinadrawer.cookbook.domain.CookBook.Ingredient
import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import com.stuckinadrawer.cookbook.domain.PostgresConfig
import doobie.util.transactor.Transactor.Aux
import doobie.postgres._
import doobie.postgres.implicits._

import scala.concurrent.ExecutionContext

class DBTest(config: PostgresConfig) {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  lazy val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    config.driver,
    config.url,
    config.user,
    config.pass
  )

  case class SomeDbValue(name: String)

  def execute[A](c: ConnectionIO[A]): A = c.transact(xa).unsafeRunSync
  def execute2[A](c: ConnectionIO[A])   = c.transact(xa)

  def find(): ConnectionIO[Option[SomeDbValue]] =
    sql"select name from test_book limit 1".query[SomeDbValue].option

  case class ParsedRecipe(id: Long,
                          name: String,
                          ingredients: List[Ingredient],
                          instructions: String)

  def createRecipe(name: String,
                   ingredients: List[Ingredient],
                   instructions: String): ConnectionIO[Int] =
    sql"insert into recipe(name, ingredients, instructions) values ($name, $ingredients,$instructions)".update.run

  def createRecipe2(name: String,
                    ingredients: List[Ingredient],
                    instructions: String): ConnectionIO[ParsedRecipe] =
    sql"insert into recipe(name, ingredients, instructions) values ($name, $ingredients,$instructions)".update
      .withUniqueGeneratedKeys[ParsedRecipe]("id", "name", "ingredients", "instructions")

  def readRecipes(): ConnectionIO[List[ParsedRecipe]] =
    sql"select id, name, ingredients, instructions from recipe"
      .query[ParsedRecipe]
      .to[List] //why?!

  def readNames() =
    sql"select name from recipe"
      .query[String]
      .to[List]

  val program3: ConnectionIO[(Int, Double)] =
    for {
      a <- sql"select 42".query[Int].unique
      b <- sql"select random()".query[Double].unique
    } yield (a, b)

  val program3a: doobie.ConnectionIO[(Int, Double)] = {
    val a: ConnectionIO[Int]    = sql"select 42".query[Int].unique
    val b: ConnectionIO[Double] = sql"select random()".query[Double].unique
    (a, b).tupled
  }

  val x: ConnectionIO[List[(Int, Double)]] = program3a.replicateA(6)
}
