package com.stuckinadrawer.cookbook.boot

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

        db.tryToRead() match {
          case Some(value) => println(s"yay: $value")
          case None        => println(s"meh.failed")
        }
      } catch {
        case t: Throwable => println(s" very meh: $t")
      }

  }

  def fly(conf: ServiceConf) = {
    val f = Flyway
      .configure()
      .dataSource(conf.postgres.url, conf.postgres.user, conf.postgres.pass)
      .load()
    f.migrate()
  }

}
