import sbt._
object Dependencies {
  val zio = "dev.zio" %% "zio" % Version.zio

  val zioCatsInterop = "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC12"

  val pureConfig = "com.github.pureconfig" %% "pureconfig" % Version.pureConfig

  val doobie = Seq(
    "org.tpolecat" %% "doobie-core"     % Version.doobie,
    "org.tpolecat" %% "doobie-postgres" % Version.doobie,
    "org.tpolecat" %% "doobie-specs2"   % Version.doobie
  )

  val http4s = Seq(
    "org.http4s" %% "http4s-blaze-server" % Version.http4s,
    "org.http4s" %% "http4s-circe"        % Version.http4s,
    "org.http4s" %% "http4s-dsl"          % Version.http4s
  )
}
object Version {
  val zio        = "1.0.0-RC18-2"
  val doobie     = "0.8.8"
  val http4s     = "0.21.1"
  val pureConfig = "0.12.3"
}
