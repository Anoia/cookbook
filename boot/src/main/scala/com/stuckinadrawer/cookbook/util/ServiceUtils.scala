package com.stuckinadrawer.cookbook.util

import cats.effect.IO
import com.stuckinadrawer.cookbook.foodstuffs.FoodStuff.FoodStuffId
import io.circe.{Decoder, Encoder, HCursor, Json}
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
  implicit val encodeFoodStuffId: Encoder[FoodStuffId] = (a: FoodStuffId) => Json.fromInt(a.value)
  implicit val decodeFoodStuffId: Decoder[FoodStuffId] = (c: HCursor) =>
    for (i <- c.as[Int]) yield FoodStuffId(i)

}
