import sbt._
import Settings._

lazy val boot = project
  .settings(commonSettings)
  .settings(libraryDependencies ++= bootDependencies,
            testFrameworks += new TestFramework("munit.Framework"))
// .dependsOn(service)

lazy val cookbook = project
  .in(file("."))
  .settings(commonSettings)
  .settings(moduleName := "cookbook")
  .settings(name := "cookbook")
  .dependsOn(boot)
  .aggregate(boot)
  .enablePlugins(JavaAppPackaging)

Compile / mainClass := Some("com.stuckinadrawer.cookbook.boot.Boot")
