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

package forms.mappings

import models.event24.TypeOfProtectionGroup1
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.domain.Nino
import utils.CountryOptions

import java.time.LocalDate
import scala.language.implicitConversions

trait Constraints {

  val regexName = """^[a-zA-Z &`\-\'\.^]{1,35}$"""
  val regexPersonOrOrgName = """^[a-zA-Z &`\'\.^\\]{0,160}$"""
  val regexMemberRecipientName = """^[a-zA-Z &`\\\-\'\.^]{0,150}$"""
  val regexPostcode = """^[A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2}$"""
  val regexAddressLine = """^[A-Za-z0-9 &!'‘’(),./—–‐-]{1,35}$"""
  val regexSafeText = """^[a-zA-Z0-9À-ÿ !#$%&'‘’"“”«»()*+,./:;=?@\\\[\]|~£€¥\—–‐_^`-]{1,160}$"""
  val regexTightText = """^[a-zA-ZàÀ-ÿ '&.^-]{1,160}$"""
  val regexTightTextWithNumber = """^[a-zA-Z0-9àÀ-ÿ '&.^-]{1,160}$"""
  val regexEvent1Description = """^[a-zA-Z0-9À-ÿ !#$%&'‘’"“”«»()*+,./:;=?@\\\[\]£€¥\—–‐-]{1,}$"""
  val regexCrn = "^[A-Za-z0-9 -]{7,8}$"
  val inputProtectionTypeRegex = "^[A-Za-z0-9]{8,15}$"
  val psaIdRegex = "^A[0-9]{7}$"
  val employerIdRefDigitsRegex = "^[0-9]{3}[0-9a-zA-Z/]{5,8}$"
  val employerIdRefNoSlashRegex = "^[0-9]{3}/[0-9a-zA-Z]{5,8}"
  val employerIdRefDisallowedCharsRegex = "[A-Za-z0-9/]{9,12}"
  val protectionReferenceRegex = "^[A-Za-z0-9]{8,15}$"
  val MAX_LENGTH = 160

  protected def postCode(errorKey: String): Constraint[String] = regexp(regexPostcode, errorKey)

  protected def addressLine(errorKey: String): Constraint[String] = regexp(regexAddressLine, errorKey)

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint {
      input =>
        constraints
          .map(_.apply(input))
          .find(_ != Valid)
          .getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input >= minimum) {
          Valid
        } else {
          Invalid(errorKey, minimum)
        }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, maximum)
        }
    }

  protected def zeroValue[A](zero: A, errorKey: String): Constraint[A] =
    Constraint {
      input =>
        if (input != zero) Valid else Invalid(errorKey, zero)
    }

  protected def safeText(errorKey: String): Constraint[String] = regexp(regexSafeText, errorKey)

  protected def tightText(errorKey: String): Constraint[String] = regexp(regexTightText, errorKey)

  protected def tightTextWithNumber(errorKey: String): Constraint[String] = regexp(regexTightTextWithNumber, errorKey)

  protected def companyNumber(errorKey: String): Constraint[String] = regexp(regexCrn, errorKey)

  protected def country(countryOptions: CountryOptions, errorKey: String): Constraint[String] = {
    Constraint {
      input =>
        countryOptions.options
          .find(_.value == input)
          .map(_ => Valid)
          .getOrElse(Invalid(errorKey))
    }
  }

  protected def employerPayeRefDigits(errorKey: String): Constraint[String] = regexp(employerIdRefDigitsRegex, errorKey)

  protected def employerPayeRefNoSlash(errorKey: String): Constraint[String] = regexp(employerIdRefNoSlashRegex, errorKey)

  protected def employerIdRefDisallowedChars(errorKey: String): Constraint[String] = regexp(employerIdRefDisallowedCharsRegex, errorKey)

  implicit def convertToOptionalConstraint[T](constraint: Constraint[T]): Constraint[Option[T]] =
    Constraint {
      case Some(t) => constraint.apply(t)
      case _ => Valid
    }

  protected def inRange[A](minimum: A, maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input >= minimum && input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, minimum, maximum)
        }
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }

  protected def minLength(minimum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length >= minimum =>
        Valid
      case _ =>
        Invalid(errorKey, minimum)
    }

  protected def maxDate(maximum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isAfter(maximum) =>
        Invalid(errorKey, args*)
      case _ =>
        Valid
    }

  protected def minDate(minimum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isBefore(minimum) =>
        Invalid(errorKey, args*)
      case _ =>
        Valid
    }

  protected def nonEmptySet(errorKey: String): Constraint[Set[?]] =
    Constraint {
      case set if set.nonEmpty =>
        Valid
      case _ =>
        Invalid(errorKey)
    }

  protected def protectionGroup1Constraint(errorKey: String): Constraint[Set[TypeOfProtectionGroup1]] =
    Constraint {
      case set if set.contains(TypeOfProtectionGroup1.NoneOfTheAbove) =>
          if(set.size == 1){
            Valid
          } else {
            Invalid(errorKey)
          }
      case _ => Valid
    }

  protected def validNino: Constraint[String] = Constraint { input =>

    val conditionsAndErrors: List[(String => Boolean, String)] = List(
      (_.length != 9, "genericNino.error.invalid.length"),
      (!_.take(2).matches("[a-zA-Z]{2}"), "genericNino.error.invalid.prefix"),
      (!_.substring(2, 8).matches("[0-9]{6}"), "genericNino.error.invalid.numbers"),
      (!_.takeRight(1).matches("[a-zA-Z]{1}"), "genericNino.error.invalid.suffix"),
      (!Nino.isValid(_), "genericNino.error.invalid")
    )

    conditionsAndErrors.collectFirst {
      case (condition, error) if condition(input) => Invalid(error)
    }.getOrElse(Valid)
  }

  protected def yearHas4Digits(errorKey: String): Constraint[LocalDate] =
    Constraint {
      case date if date.getYear >= 1000 => Valid
      case _ => Invalid(errorKey,"year")
    }

  protected def isEqual(expectedValue: Option[String], errorKey: String): Constraint[String] =
    Constraint {
      case _ if expectedValue.isEmpty => Valid
      case s if expectedValue.contains(s) => Valid
      case _ => Invalid(errorKey)
    }

  protected def isNotBeforeOpenDate(openDate: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isBefore(openDate) =>
        Invalid(errorKey, args*)
      case _ =>
        Valid
    }

  protected def descriptionConstraint(lengthError: String, invalidCharacters: String): Constraint[String] =
    Constraint("constraints.benefitsPaidEarly") { value =>
    val isLengthValid = value.length <= MAX_LENGTH
    val isRegexValid = value.matches(regexEvent1Description)

    (isLengthValid, isRegexValid) match {
      case (false, false) => Invalid("description.error.invalid")
      case (false, true)  => Invalid(lengthError, MAX_LENGTH)
      case (true, false)  => Invalid(invalidCharacters)
      case (true, true)   => Valid
    }
  }
}
