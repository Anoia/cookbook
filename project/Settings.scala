import Dependencies._
import sbt._
import sbt.Keys.{scalacOptions, _}

object Settings {

  val commonSettings = {
    Seq(
      scalaVersion := "2.13.1",
      scalacOptions := Seq(
        "-encoding",
        "utf-8",
        "-deprecation",
        "-feature",
        "-unchecked",
        "-explaintypes",
        "-language:postfixOps",
        "-language:implicitConversions",
        "-Xcheckinit",
        "-Xfatal-warnings"
      ),
      version := (version in ThisBuild).value
    )

  }

  val storageDependencies: List[ModuleID]     = List(zio) ++ doobie ++ flyway
  val serviceDependencies: List[sbt.ModuleID] = storageDependencies ++ http4s ++ circe
  val bootDependencies: List[sbt.ModuleID] =
    serviceDependencies ++ Seq(pureConfig) ++ Seq(scalaMeta)
}
