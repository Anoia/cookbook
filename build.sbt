import sbt._
import Settings._

lazy val domain = project
  .settings(commonSettings)

lazy val storage = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= storageDependencies)
  .dependsOn(domain)

lazy val service = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= serviceDependencies)
  .dependsOn(storage)

lazy val boot = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= bootDependencies)
  .dependsOn(service)

lazy val cookbook = project
  .in(file("."))
  .settings(commonSettings)
  .settings(moduleName := "cookbook")
  .settings(name := "cookbook")
  .aggregate(domain, storage, service, boot)
