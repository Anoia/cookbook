package com.stuckinadrawer.cookbook.boot

import pureconfig._
import pureconfig.generic.auto._

object Boot extends App {

  println("hello")

  ConfigSource.default.load[ServiceConf] match {
    case Left(value) => println(s"config loading failed: $value")
    case Right(conf) =>
      println(s"config loading successful: $conf")

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

}
