import sbt._
import Settings._

lazy val boot = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= bootDependencies)
// .dependsOn(service)

lazy val cookbook = project
  .in(file("."))
  .settings(commonSettings)
  .settings(moduleName := "cookbook")
  .settings(name := "cookbook")
  .aggregate(boot)
