/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.domain.Nino
import utils.CountryOptions

import java.time.LocalDate

trait Constraints {

  val regexName = """^[a-zA-Z &`\-\'\.^]{1,35}$"""
  val regexPersonOrOrgName = """^[a-zA-Z &`\'\.^\\]{0,160}$"""
  val regexSurname = """^[a-zA-Z &`\\\-\'\.^]{1,35}$"""
  val regexMemberRecipientName = """^[a-zA-Z &`\\\-\'\.^]{0,150}$"""
  val regexPostcode = """^[A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2}$"""
  val regexPostCodeNonUk = """^([0-9]+-)*[0-9]+$"""
  val regexAddressLine = """^[A-Za-z0-9 &!'‘’(),./\u2014\u2013\u2010\u002d]{1,35}$"""
  val regexSafeText = """^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’"“”«»()*+,./:;=?@\\\[\]|~£€¥\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,160}$"""
  val regexCrn = "^[A-Za-z0-9 -]{7,8}$"

  protected def postCode(errorKey: String): Constraint[String] = regexp(regexPostcode, errorKey)

  protected def postCodeNonUk(errorKey: String): Constraint[String] = regexp(regexPostCodeNonUk, errorKey)

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

  protected def nonNegValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
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

  protected def safeText(errorKey: String): Constraint[String] = regexp(regexSafeText, errorKey)
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
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def minDate(minimum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isBefore(minimum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def nonEmptySet(errorKey: String): Constraint[Set[_]] =
    Constraint {
      case set if set.nonEmpty =>
        Valid
      case _ =>
        Invalid(errorKey)
    }

  protected def validNino(invalidKey: String): Constraint[String] = {
    Constraint {
      case nino if Nino.isValid(nino) => Valid
      case _ => Invalid(invalidKey)
    }
  }

  protected def yearHas4Digits(errorKey: String): Constraint[LocalDate] =
    Constraint {
      case date if date.getYear >= 1000 => Valid
      case _ => Invalid(errorKey)
    }
}
