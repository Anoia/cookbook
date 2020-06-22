package com.stuckinadrawer.cookbook.boot

import cats.effect._
import cats.implicits._
import com.stuckinadrawer.cookbook.foodstuffs.{DoobieFoodStuffRepository, FoodStuffService}
import com.stuckinadrawer.cookbook.recipes.RecipeService
import com.stuckinadrawer.cookbook.recipes.DoobieRecipeRepository
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
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

  def getConfigFromEnvIfSet(fileCfg: PostgresConfig): PostgresConfig = {
    (for {
      url  <- sys.env.get("JDBC_DATABASE_URL")
      user <- sys.env.get("JDBC_DATABASE_USERNAME")
      pass <- sys.env.get("JDBC_DATABASE_PASSWORD")
    } yield PostgresConfig(fileCfg.driver, url, user, pass)).getOrElse(fileCfg)
  }

  def createTransactor(cfg: PostgresConfig): Resource[IO, HikariTransactor[IO]] = {
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32)
      be <- Blocker[IO]
      xa <- HikariTransactor
        .newHikariTransactor[IO](
          cfg.driver,
          cfg.url,
          cfg.user,
          cfg.pass,
          ce,
          be
        )
    } yield xa
  }

  def serverBuilder(xa: HikariTransactor[IO])(cfg: HttpConfig): BlazeServerBuilder[IO] = {
    val recipeService = new RecipeService(new DoobieRecipeRepository(xa).recipeRepository).recipeRoutes
    val foodStuffService = new FoodStuffService(
      new DoobieFoodStuffRepository(xa).foodStuffRepository).foodStuffRoutes

    val corsConfig = CORSConfig(anyOrigin = false,
                                allowedOrigins = cfg.allowedOrigins,
                                allowCredentials = false,
                                maxAge = 1.day.toSeconds)

    val requestLogger = com.typesafe.scalalogging.Logger("request")

    val httpApp = CORS(Router("/" -> (recipeService <+> foodStuffService)).orNotFound, corsConfig)
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
      dbCfg = getConfigFromEnvIfSet(cfg.postgres)
      _ <- migrateDB(dbCfg)
      transactor = createTransactor(dbCfg)
      test <- transactor.use(xa => serverBuilder(xa)(cfg.http).resource.use(_ => IO.never))
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
