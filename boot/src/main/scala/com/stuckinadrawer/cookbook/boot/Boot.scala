package com.stuckinadrawer.cookbook.boot

import com.stuckinadrawer.cookbook.domain.ServiceConf
import org.flywaydb.core.Flyway
import pureconfig._
import pureconfig.generic.auto._

object Boot extends App {

  println("hello")

  ConfigSource.default.load[ServiceConf] match {
    case Left(value) => println(s"config loading failed: $value")
    case Right(conf) =>
      println(s"config loading successful: $conf")

      try {
        fly(conf)
      } catch {
        case t: Throwable => println(s" flyway: $t")
      }

      try {
        val db = new DBTest(conf.postgres)

        // val r = Recipe("pasta", List("pasta, tomatoes"), "cook and combine")

        // db.execute(db.createRecipe("salad", List("cucumber, salad"), "cut and toss"))

        val recipe = db.execute(db.createRecipe2("steak", List("steak", "steak"), "grill it!"))

        println(s"Added a new recipe: $recipe")

        val r = db.execute(db.readRecipes())
        r.map(value => println(s"yay: $value"))

      } catch {
        case t: Throwable => println(s" very meh: $t")
      }

  }

  def fly(conf: ServiceConf) =
    Flyway
      .configure()
      .dataSource(conf.postgres.url, conf.postgres.user, conf.postgres.pass)
      .load()
      .migrate()

}
