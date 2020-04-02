package com.stuckinadrawer.cookbook.boot
import doobie._
import doobie.implicits._
import cats.effect.IO
import scala.concurrent.ExecutionContext

class DBTest(config: PostgresConfig) {

  implicit val cs = IO.contextShift(ExecutionContext.global)

  lazy val xa = Transactor.fromDriverManager[IO](
    config.driver,
    config.url,
    config.user,
    config.pass
  )

  case class SomeDbValue(name: String)

  def tryToRead(): Option[SomeDbValue] = {
    find().transact(xa).unsafeRunSync
  }

  def find(): ConnectionIO[Option[SomeDbValue]] =
    sql"select name from testbook limit 1".query[SomeDbValue].option
}
