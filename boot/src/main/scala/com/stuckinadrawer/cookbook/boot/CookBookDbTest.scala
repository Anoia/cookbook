package com.stuckinadrawer.cookbook.boot

import cats.effect._
import cats.implicits._
import com.stuckinadrawer.cookbook.domain.CookBook.{NewRecipe, RecipePatch}
import com.stuckinadrawer.cookbook.domain.{PostgresConfig, ServiceConf}
import com.stuckinadrawer.cookbook.service.RecipeService
import com.stuckinadrawer.cookbook.storage.{DoobieRecipeRepository, RecipeRepository}
import org.flywaydb.core.Flyway
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze._
import org.http4s.server.middleware.Logger
import pureconfig.generic.auto._
import pureconfig.ConfigSource

object CookBookDbTest extends IOApp {

  def loadConfig: IO[ServiceConf] = IO.fromEither {
    ConfigSource.default.load[ServiceConf].leftMap(e => new IllegalStateException(e.toString))
  }

  def serverBuilder(rr: RecipeRepository.Service): BlazeServerBuilder[IO] = {
    val services = new RecipeService(rr).recipeRoutes

    val httpApp = Router("/" -> services, "/api" -> services).orNotFound
    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(Logger.httpApp(logHeaders = true, logBody = true)(httpApp))
  }

  override def run(args: List[String]): IO[ExitCode] = {

    val dbResource = for {
      cfg <- loadConfig
      _   <- migrateDB(cfg.postgres)
      repo = DoobieRecipeRepository.createRecipeRepository(cfg.postgres)
      test <- repo.use(r => serverBuilder(r).resource.use(_ => IO.never))
    } yield {
      test
    }

    dbResource.as(ExitCode.Success)
  }

  def testDb(repo: RecipeRepository.Service): IO[String] =
    for {
      //  _ <- repo.create(NewRecipe("steak", List("meat", "meat"), "fry it!"))
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
