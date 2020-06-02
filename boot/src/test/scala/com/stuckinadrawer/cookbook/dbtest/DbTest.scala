package com.stuckinadrawer.cookbook.dbtest

import java.time.OffsetDateTime

import cats.effect.{IO, _}
import com.stuckinadrawer.cookbook.recipes.DoobieRecipeRepository.SQL
import com.stuckinadrawer.cookbook.recipes.Recipe.{NewRecipe, Recipe, RecipeId}
import doobie._
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAll

class DbTest extends Specification with doobie.specs2.IOChecker with BeforeAll {
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  val url  = "jdbc:postgresql://localhost:5432/cookbook"
  val user = "cookbook"
  val pass = "cookbook"

  override def transactor: doobie.Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    url,
    user,
    pass,
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )

  check(SQL.create(NewRecipe("", "", List.empty, "")))
  // checkOutput(SQL.get(RecipeId(1)))
  check(SQL.getAll(Some("")))
  check(
    SQL.update(
      Recipe(RecipeId(1), "", "", List.empty, "", OffsetDateTime.now(), OffsetDateTime.now())))
  check(SQL.delete(RecipeId(1)))

  override def beforeAll(): Unit = {
    Flyway
      .configure()
      .dataSource(url, user, pass)
      .load()
      .migrate()
    ()
  }
}
