import Dependencies._
import sbt._
import sbt.Keys._

object Settings {

  val commonSettings = {
    Seq(
      scalaVersion := "2.13.2",
      //    scalacOptions += "-Wconf:any:warning-verbose",
      version := (version in ThisBuild).value
    )

  }

  val storageDependencies: List[ModuleID]     = List(zio) ++ doobie ++ flyway
  val serviceDependencies: List[sbt.ModuleID] = storageDependencies ++ http4s ++ circe
  val bootDependencies: List[sbt.ModuleID] =
    serviceDependencies ++ Seq(pureConfig) ++ Seq(scalaMeta)
}
