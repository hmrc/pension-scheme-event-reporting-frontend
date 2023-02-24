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

package models.requests

import models.{LoggedInUser, UserAnswers}
import play.api.mvc.{Request, WrappedRequest}

abstract class RequiredSchemeDataRequest[A](request: Request[A]) extends WrappedRequest[A](request) {
  def pstr: String
  def schemeName: String
  def returnUrl: String
  def loggedInUser: LoggedInUser
}

case class OptionalDataRequest[A](
                                   pstr: String,
                                   schemeName: String,
                                   returnUrl: String,
                                   request: Request[A],
                                   loggedInUser: LoggedInUser,
                                   userAnswers: Option[UserAnswers]
                                 ) extends RequiredSchemeDataRequest[A](request)

case class DataRequest[A](pstr: String,
                          schemeName: String,
                          returnUrl: String,
                          request: Request[A],
                          loggedInUser: LoggedInUser,
                          userAnswers: UserAnswers
                         ) extends RequiredSchemeDataRequest[A](request)
