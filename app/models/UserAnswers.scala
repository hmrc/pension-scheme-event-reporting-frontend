/*
 * Copyright 2023 HM Revenue & Customs
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

import models.enumeration.EventType
import pages.{QuestionPage, TaxYearPage}
import play.api.libs.json._
import queries.{Derivable, Gettable, Query, Settable}

import scala.util.{Failure, Success, Try}

final case class UserAnswers(
                              data: JsObject = Json.obj(),
                              noEventTypeData: JsObject = Json.obj()
                            ) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] = {
    Reads.optionNoError(Reads.at(page.path)).reads(data) match {
      case JsSuccess(a@Some(_), _) => a
      case _ =>
        Reads.optionNoError(Reads.at(page.path)).reads(noEventTypeData).getOrElse(None)
    }
  }

  def get(path: JsPath)(implicit rds: Reads[JsValue]): Option[JsValue] =
    Reads.optionNoError(Reads.at(path)).reads(data) match {
      case JsSuccess(a@Some(_), _) => a
      case _ =>
        Reads.optionNoError(Reads.at(path)).reads(noEventTypeData).getOrElse(None)
    }

  def get[A, B](derivable: Derivable[A, B])(implicit rds: Reads[A]): Option[B] =
    Reads.optionNoError(Reads.at(derivable.path))
      .reads(data)
      .getOrElse(None)
      .map(derivable.derive)

  def isDefined(gettable: Gettable[_]): Boolean = {
    Reads.optionNoError(Reads.at[JsValue](gettable.path)).reads(data) match {
      case JsSuccess(a@Some(_), _) if a.isDefined => true
      case _ =>
        Reads.optionNoError(Reads.at[JsValue](gettable.path)).reads(noEventTypeData)
            .map(_.isDefined)
            .getOrElse(false)
    }
  }

  def set[A](page: Settable[A], value: A, nonEventTypeData: Boolean = false)(implicit writes: Writes[A]): Try[UserAnswers] = {
    def dataToUpdate(ua: UserAnswers): JsObject = if (nonEventTypeData) ua.noEventTypeData else ua.data
    page.cleanupBeforeSettingValue(Some(value), this).flatMap { ua =>
      val updatedData = dataToUpdate(ua).setObject(page.path, Json.toJson(value)) match {
        case JsSuccess(jsValue, _) =>
          Success(jsValue)
        case JsError(errors) =>
          Failure(JsResultException(errors))
      }

      updatedData.flatMap {
        d =>
          val updatedAnswers =
            if (nonEventTypeData) {
              copy(noEventTypeData = d)
            } else {
              copy(data = d)
            }
          page.cleanup(Some(value), updatedAnswers)
      }
    }
  }

  def set(path: JsPath, value: JsValue): Try[UserAnswers] = {
    val updatedData = data.setObject(path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        Success(updatedAnswers)
    }
  }

  def setOrException[A](page: QuestionPage[A], value: A, nonEventTypeData: Boolean = false)(implicit writes: Writes[A]): UserAnswers = {
    set(page, value, nonEventTypeData) match {
      case Success(ua) => ua
      case Failure(ex) => throw ex
    }
  }

  def setOrException(path: JsPath, value: JsValue): UserAnswers = set(path, value) match {
    case Success(ua) => ua
    case Failure(ex) => throw ex
  }

  def remove[A](page: Settable[A], nonEventTypeData: Boolean = false): Try[UserAnswers] = {
    def dataToUpdate(ua: UserAnswers): JsObject = if (nonEventTypeData) ua.noEventTypeData else ua.data
    val updatedData = dataToUpdate(this).removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = if (nonEventTypeData) {
          copy(noEventTypeData = d)
        } else {
          copy(data = d)
        }
        page.cleanup(None, updatedAnswers)
    }
  }

  def removeOrException[A](page: QuestionPage[A], nonEventTypeData: Boolean = false): UserAnswers = {
    remove(page, nonEventTypeData) match {
      case Success(ua) => ua
      case Failure(ex) => throw ex
    }
  }

  def getAll[A](page: Gettable[Seq[A]])(implicit reads: Reads[A]): Seq[A] =
    data.as[Option[Seq[A]]](page.path.readNullable[Seq[A]]).toSeq.flatten

  def countAll(page: Query): Int =
    page.path.readNullable[JsArray].reads(data).asOpt.flatten.map(_.value.size).getOrElse(0)

  def sumAll(page: Query, readsBigDecimal: Reads[BigDecimal]): BigDecimal = {
    def zeroValue = BigDecimal(0)

    page.path.readNullable[JsArray].reads(data).asOpt.flatten
      .map(_.value.map(jsValue => readsBigDecimal.reads(jsValue).asOpt.getOrElse(zeroValue)).sum)
      .getOrElse(zeroValue)
  }

  def eventDataIdentifier(eventType: EventType): EventDataIdentifier = {
    (noEventTypeData \ TaxYearPage.toString).asOpt[String] match {
      case Some(year) => EventDataIdentifier(eventType, year, "1")
      case _ => throw new RuntimeException("No tax year available")
    }
  }
}
