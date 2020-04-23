package com.stuckinadrawer.cookbook.boot

import cats.effect._
import com.stuckinadrawer.bookbook.domain.{PostgresConfig, ServiceConf}
import com.stuckinadrawer.cookbook.storage.{DoobieRecipeRepository, RecipeRepository}
import org.flywaydb.core.Flyway
import pureconfig.ConfigSource
import cats.implicits._
import com.stuckinadrawer.bookbook.domain.CookBook.{NewRecipe, RecipePatch}
import doobie.postgres._
import doobie.postgres.implicits._
import pureconfig.generic.auto._

object CookBookDbTest extends IOApp {

  def loadConfig: IO[ServiceConf] = IO.fromEither {
    ConfigSource.default.load[ServiceConf].leftMap(e => new IllegalStateException(e.toString))
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val program = for {
      cfg <- loadConfig
      _   <- migrateDB(cfg.postgres)
      r   <- DoobieRecipeRepository.createRecipeRepository(cfg.postgres).use(testDb)
    } yield {
      println(r)
    }

    program.attempt.map {
      case Left(value) =>
        println(s"program execution failed: $value")
        ExitCode.Error
      case Right(_) =>
        println(s"program execution successful!")
        ExitCode.Success
    }
  }

  def testDb(repo: RecipeRepository.Service): IO[String] =
    for {
      _ <- repo.create(NewRecipe("steak", List("meat", "meat"), "fry it!"))
      secondRecipe <- repo.create(
        NewRecipe("salad", List("cucumber", "tomato", "salad"), "toss it"))
      _   <- repo.update(secondRecipe.id, RecipePatch(name = Some("cucumber tomato salad")))
      all <- repo.getAll
    } yield {
      all.map(r => s"$r").mkString(" ")
    }

  def migrateDB(cfg: PostgresConfig): IO[Int] =
    IO {
      Flyway
        .configure()
        .dataSource(cfg.url, cfg.user, cfg.pass)
        .load()
        .migrate()
    }
}
