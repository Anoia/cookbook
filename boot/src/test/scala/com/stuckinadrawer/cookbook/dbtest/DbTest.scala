package com.stuckinadrawer.cookbook.dbtest

import cats.effect.{Blocker, IO}
import doobie.ExecutionContexts
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAll

trait DbTest extends Specification with doobie.specs2.IOChecker with BeforeAll {
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  val url  = "jdbc:postgresql://localhost:5432/cookbook"
  val user = "cookbook"
  val pass = "cookbook"

  override def transactor: doobie.Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    url,
    user,
    pass,
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )
  override def beforeAll(): Unit = {
    Flyway
      .configure()
      .dataSource(url, user, pass)
      .load()
      .migrate()
    ()
  }

}
