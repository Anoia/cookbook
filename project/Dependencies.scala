import sbt._
object Dependencies {
  val zio = "dev.zio" %% "zio" % Version.zio
  val doobie = Seq(
    "org.tpolecat" %% "doobie-core"     % Version.doobieVersion,
    "org.tpolecat" %% "doobie-postgres" % Version.doobieVersion,
    "org.tpolecat" %% "doobie-specs2"   % Version.doobieVersion
  )
}
object Version {
  val zio                = "1.0.0-RC18-2"
  lazy val doobieVersion = "0.8.8"
}
