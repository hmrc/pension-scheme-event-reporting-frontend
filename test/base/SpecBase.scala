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

package base

import controllers.actions._
import models.{TaxYear, UserAnswers}
import models.requests.OptionalDataRequest
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.TaxYearPage
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.SessionKeys
import utils.RequiredDataRequest

import scala.language.implicitConversions

trait SpecBase
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience {

  val userAnswersId: String = "id"


  def fakeRequest = FakeRequest("", "").withSession(
    SessionKeys.sessionId -> "sessionId"
  )

  implicit def fakeRequestToRequiredDataRequest[A](fakeRequest: FakeRequest[A]): OptionalDataRequest[A] =
    RequiredDataRequest.optionalDataRequest(fakeRequest)

  def emptyUserAnswers: UserAnswers = UserAnswers()
  def emptyUserAnswersWithTaxYear: UserAnswers = UserAnswers().set(TaxYearPage, TaxYear("2022"), nonEventTypeData = true).get
  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())


  private val app = applicationBuilder(None).build()

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None,
                                   extraModules: Seq[GuiceableModule] = Seq.empty): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        extraModules ++ Seq[GuiceableModule](
          bind[DataRequiredAction].to[DataRequiredActionImpl],
          bind[IdentifierAction].to[FakeIdentifierAction],
          bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
        ): _*
      )

  protected def applicationBuilderForPSP(userAnswers: Option[UserAnswers] = None,
                                   extraModules: Seq[GuiceableModule] = Seq.empty): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        extraModules ++ Seq[GuiceableModule](
          bind[DataRequiredAction].to[DataRequiredActionImpl],
          bind[IdentifierAction].to[FakeIdentifierAction],
          bind[DataRetrievalAction].toInstance(new FakeDataRetrievalActionForPSP(userAnswers))
        ): _*
      )

  protected def messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  protected implicit def messages: Messages = messagesApi.preferred(fakeRequest)

}
