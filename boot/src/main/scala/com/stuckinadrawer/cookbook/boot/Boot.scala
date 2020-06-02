package com.stuckinadrawer.cookbook.boot

import cats.effect._
import cats.implicits._
import com.stuckinadrawer.cookbook.recipes.{RecipeRepository, RecipeService}
import com.stuckinadrawer.cookbook.recipes.DoobieRecipeRepository
import org.flywaydb.core.Flyway
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze._
import org.http4s.server.middleware.{CORS, CORSConfig, Logger}
import pureconfig.generic.auto._
import pureconfig.ConfigSource

import scala.annotation.nowarn
import scala.concurrent.duration._

object Boot extends IOApp {

  def loadConfig: IO[ServiceConf] = IO.fromEither {
    ConfigSource.default.load[ServiceConf].leftMap(e => new IllegalStateException(e.toString))
  }

  def serverBuilder(repo: RecipeRepository.Service)(cfg: HttpConfig): BlazeServerBuilder[IO] = {
    val services = new RecipeService(repo).recipeRoutes

    val corsConfig = CORSConfig(anyOrigin = false,
                                allowedOrigins = cfg.allowedOrigins,
                                allowCredentials = false,
                                maxAge = 1.day.toSeconds)

    val requestLogger = com.typesafe.scalalogging.Logger("request")

    val httpApp = CORS(Router("/" -> services).orNotFound, corsConfig)
    BlazeServerBuilder[IO]
      .bindHttp(cfg.port, cfg.host)
      .withHttpApp(
        Logger.httpApp[IO](logHeaders = true,
                           logBody = true,
                           logAction = Some(s => IO.pure(requestLogger.info(s))))(httpApp))
  }

  override def run(args: List[String]): IO[ExitCode] = {

    @nowarn("cat=w-flag-dead-code")
    val dbResource: IO[Any] = for {
      cfg <- loadConfig
      _   <- migrateDB(cfg.postgres)
      repo = DoobieRecipeRepository.createRecipeRepository(cfg.postgres)
      test <- repo.use(r => serverBuilder(r)(cfg.http).resource.use(_ => IO.never))
    } yield {
      test
    }

    dbResource.as(ExitCode.Success)
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
