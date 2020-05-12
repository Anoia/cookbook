package com.stuckinadrawer.cookbook.test

import cats.effect.IO
import org.http4s.{EntityDecoder, Response, Status}

trait Http4sTestHelper extends munit.FunSuite {

  def check[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])(
      implicit ev: EntityDecoder[IO, A]
  ): Unit = {
    val actualResp = actual.unsafeRunSync
    assertEquals(actualResp.status, expectedStatus)
    expectedBody match {
      case Some(value) => assertEquals(actualResp.as[A].unsafeRunSync, value)
      case None        => assert(actualResp.body.compile.toVector.unsafeRunSync.isEmpty)
    }
  }

}
