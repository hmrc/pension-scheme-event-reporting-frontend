/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package generators

import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.{Gen, Shrink}

import java.time.{Instant, LocalDate, ZoneOffset}
import scala.math.BigDecimal.RoundingMode

trait Generators extends PageGenerators with ModelGenerators with UserAnswersEntryGenerators {

  implicit val dontShrink: Shrink[String] = Shrink.shrinkAny

  def genIntersperseString(gen: Gen[String],
                           value: String,
                           frequencyV: Int = 1,
                           frequencyN: Int = 10): Gen[String] = {

    val genValue: Gen[Option[String]] = Gen.frequency(frequencyN -> None, frequencyV -> Gen.const(Some(value)))

    for {
      seq1 <- gen
      seq2 <- Gen.listOfN(seq1.length, genValue)
    } yield {
      seq1.toSeq.zip(seq2).foldLeft("") {
        case (acc, (n, Some(v))) =>
          acc + n + v
        case (acc, (n, _)) =>
          acc + n
      }
    }
  }

  def intsInRangeWithCommas(min: Int, max: Int): Gen[String] = {
    val numberGen = choose[Int](min, max).map(_.toString)
    genIntersperseString(numberGen, ",")
  }

  def decsInRangeWithCommas(min: BigDecimal, max: BigDecimal): Gen[String] = {
    val numberGen = choose[BigDecimal](min, max).map[String](_.setScale(5, RoundingMode.FLOOR).toString())
    genIntersperseString(numberGen, ",")
  }

  def intsInRange(min: Int, max: Int): Gen[String] = {
    choose[Int](min, max).map(_.toString)
  }

  def decimalsBelowValue(value: BigDecimal): Gen[String] =
    arbitrary[BigDecimal]
      .suchThat(_ < value)
      .map[String](_.setScale(2, RoundingMode.FLOOR).toString())

  def decimalsAboveValue(value: BigDecimal): Gen[String] =
    arbitrary[BigDecimal]
      .suchThat(_ > value)
      .map[String](_.setScale(2, RoundingMode.FLOOR).toString())

  def longDecimalString(length: Int): Gen[String] =
    Gen.listOfN(length, Gen.choose[Char](49.toChar, 57.toChar)).map(
      list =>
        BigDecimal(list.mkString).setScale(2, RoundingMode.FLOOR).toString
    )

  def decimalsOutsideRange(min: BigDecimal, max: BigDecimal): Gen[BigDecimal] =
    arbitrary[BigDecimal] suchThat (x => x < min || x > max)

  def intsLargerThanMaxValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (x => x > Int.MaxValue)

  def intsSmallerThanMinValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (x => x < Int.MinValue)

  def nonNumerics: Gen[String] =
    alphaStr suchThat (_.size > 0)

  def decimals: Gen[String] =
    arbitrary[BigDecimal]
      .suchThat(_.abs < Int.MaxValue)
      .suchThat(!_.isValidInt)
      .map("%f".format(_))

  def intsBelowValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ < value)

  def intsAboveValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ > value)

  def intsOutsideRange(min: Int, max: Int): Gen[Int] =
    arbitrary[Int] suchThat (x => x < min || x > max)

  def nonBooleans: Gen[String] =
    arbitrary[String]
      .suchThat(_.nonEmpty)
      .suchThat(_ != "true")
      .suchThat(_ != "false")

  def nonEmptyString: Gen[String] =
    arbitrary[String] suchThat (_.nonEmpty)

  def nonEmptyStringNoNewlines: Gen[String] =
    arbitrary[String].filter(s => s.nonEmpty && !s.contains("\n")).map(_.replace("\n", ""))

  def stringsWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      chars <- listOfN(length, arbitrary[Char])
    } yield chars.mkString

  def stringsLongerThan(minLength: Int): Gen[String] =
    for {
      base <- Gen.listOfN(minLength + 1, alphaNumChar).map(_.mkString)
      surplus <- alphaNumStr
    } yield base + surplus

  def stringsWithSpecialChars(minLength: Int): Gen[String] = {
    val specialCharGen: Gen[Char] = Gen.oneOf("!@#$%^&*()_-+=<>?/|")
    for {
      base <- Gen.listOfN(minLength + 1, specialCharGen).map(_.mkString)
    } yield base
  }

  def stringsShorterThan(maxLength: Int): Gen[String] =
    for {
      base <- Gen.listOfN(maxLength-1, alphaNumChar).map(_.mkString)
    } yield base

  def stringsExceptSpecificValues(excluded: Seq[String]): Gen[String] =
    nonEmptyString suchThat (!excluded.contains(_))

  def stringsExceptSpecificValuesAndNoNewlines(excluded: Seq[String]): Gen[String] =
    nonEmptyStringNoNewlines suchThat (!excluded.contains(_))

  def oneOf[T](xs: Seq[Gen[T]]): Gen[T] =
    if (xs.isEmpty) {
      throw new IllegalArgumentException("oneOf called on empty collection")
    } else {
      val vector = xs.toVector
      choose(0, vector.size - 1).flatMap(vector(_))
    }

  def datesBetween(min: LocalDate, max: LocalDate): Gen[LocalDate] = {

    def toMillis(date: LocalDate): Long =
      date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

    Gen.choose(toMillis(min), toMillis(max)).map {
      millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate
    }
  }

  def regexWildcardChar: Gen[Char] = {
    arbitrary[Char].retryUntil(c =>
      c.getType match {
        case Character.LINE_SEPARATOR => false
        case Character.PARAGRAPH_SEPARATOR => false
        case Character.CONTROL => false
        case _ => true
      }
    )
  }
}
