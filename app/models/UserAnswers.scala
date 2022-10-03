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

package models

import pages.QuestionPage
import play.api.libs.json._
import queries.{Derivable, Gettable, Settable}

import scala.util.{Failure, Success, Try}

final case class UserAnswers(
                              data: JsObject = Json.obj()
                            ) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def get[A, B](derivable: Derivable[A, B])(implicit rds: Reads[A]): Option[B] =
    Reads.optionNoError(Reads.at(derivable.path))
      .reads(data)
      .getOrElse(None)
      .map(derivable.derive)

  def isDefined(gettable: Gettable[_]): Boolean =
    Reads.optionNoError(Reads.at[JsValue](gettable.path)).reads(data)
      .map(_.isDefined)
      .getOrElse(false)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(Some(value), updatedAnswers)
    }
  }

  def setOrException[A](page: QuestionPage[A], value: A)(implicit writes: Writes[A]): UserAnswers = {
    set(page, value) match {
      case Success(ua) => ua
      case Failure(ex) => throw ex
    }
  }

  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(None, updatedAnswers)
    }
  }

  def removeOrException[A](page: QuestionPage[A]): UserAnswers = {
    remove(page) match {
      case Success(ua) => ua
      case Failure(ex) => throw ex
    }
  }

}
