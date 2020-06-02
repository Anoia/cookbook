package com.stuckinadrawer.cookbook.util

import cats.effect.IO
import org.http4s.{EntityEncoder, Response}
import org.http4s.dsl.io._

object ServiceUtils {

  object OptionalNameQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("name")

  def optionToResponse[A](
      o: Option[A]
  )(implicit entityEncoder: EntityEncoder[IO, A]): IO[Response[IO]] = o match {
    case Some(value) => Ok(value)
    case None        => NotFound()
  }

}
