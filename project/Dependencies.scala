import sbt._
object Dependencies {
  val zio = "dev.zio" %% "zio" % Version.zio

  val zioCatsInterop = "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC12"

  val pureConfig = "com.github.pureconfig" %% "pureconfig" % Version.pureConfig

  val doobie = Seq(
    "org.tpolecat" %% "doobie-core",
    "org.tpolecat" %% "doobie-postgres",
    "org.tpolecat" %% "doobie-specs2",
    "org.tpolecat" %% "doobie-hikari"
  ).map(_ % Version.doobie)

  val flyway = Seq(
    "org.flywaydb" % "flyway-core" % Version.flyway
  )

  val http4s = Seq(
    "org.http4s" %% "http4s-blaze-server",
    "org.http4s" %% "http4s-circe",
    "org.http4s" %% "http4s-dsl"
  ).map(_ % Version.http4s)

  val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-literal"
  ).map(_ % Version.circe)

  val scalaMeta = "org.scalameta" %% "munit" % "0.7.6" % Test

}
object Version {
  val zio        = "1.0.0-RC18-2"
  val doobie     = "0.8.8"
  val flyway     = "6.3.3"
  val http4s     = "0.21.3"
  val circe      = "0.13.0"
  val pureConfig = "0.12.3"
}
