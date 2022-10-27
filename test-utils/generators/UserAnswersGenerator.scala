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

package generators

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import pages.event1.employer.{EmployerPaymentNatureDescriptionPage, EmployerTangibleMoveablePropertyPage, UnauthorisedPaymentRecipientNamePage}
import pages.event1.member._
import pages.eventWindUp.SchemeWindUpDatePage
import play.api.libs.json.{JsValue, Json}

trait UserAnswersGenerator extends TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[_], JsValue)]] =
      arbitrary[(event23.HowAddDualAllowancePage.type, JsValue)] ::
      arbitrary[(event1.PaymentValueAndDatePage.type, JsValue)] ::
      arbitrary[(event1.member.ErrorDescriptionPage.type, JsValue)] ::
      arbitrary[(BenefitsPaidEarlyPage.type, JsValue)] ::
      arbitrary[(event1.employer.PaymentNaturePage.type, JsValue)] ::
      arbitrary[(event1.employer.CompanyDetailsPage.type, JsValue)] ::
      arbitrary[(event1.BenefitInKindBriefDescriptionPage.type, JsValue)] ::
      arbitrary[(event1.SchemeUnAuthPaySurchargeMemberPage.type, JsValue)] ::
      arbitrary[(event1.ValueOfUnauthorisedPaymentPage.type, JsValue)] ::
      arbitrary[(event1.DoYouHoldSignedMandatePage.type, JsValue)] ::
      arbitrary[(event1.WhoReceivedUnauthPaymentPage.type, JsValue)] ::
      arbitrary[(event1.HowAddUnauthPaymentPage.type, JsValue)] ::
      arbitrary[(PaymentNaturePage.type, JsValue)] ::
      arbitrary[(SchemeWindUpDatePage.type, JsValue)] ::
      arbitrary[(event18.Event18ConfirmationPage.type, JsValue)] ::
      arbitrary[(EventSummaryPage.type, JsValue)] ::
      arbitrary[(EventSelectionPage.type, JsValue)] ::
      arbitrary[(event1.member.SchemeDetailsPage.type, JsValue)] ::
      arbitrary[(event1.member.WhoWasTheTransferMadePage.type, JsValue)] ::
      arbitrary[(event1.member.UnauthorisedPaymentRecipientNamePage.type, JsValue)] ::
      arbitrary[(event1.employer.LoanDetailsPage.type, JsValue)] ::
      arbitrary[(RefundOfContributionsPage.type, JsValue)] ::
      arbitrary[(event1.member.UnauthorisedPaymentRecipientNamePage.type, JsValue)] ::
      arbitrary[(RefundOfContributionsPage.type, JsValue)] ::
      arbitrary[(EmployerPaymentNatureDescriptionPage.type, JsValue)] ::
      arbitrary[(MemberPaymentNatureDescriptionPage.type, JsValue)] ::
      arbitrary[(EmployerTangibleMoveablePropertyPage.type, JsValue)] ::
      arbitrary[(MemberTangibleMoveablePropertyPage.type, JsValue)] ::
      arbitrary[(ReasonForTheOverpaymentOrWriteOffPage.type, JsValue)] ::
      arbitrary[(UnauthorisedPaymentRecipientNamePage.type, JsValue)] ::
      Nil

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        data <- generators match {
          case Nil => Gen.const(Map[QuestionPage[_], JsValue]())
          case _ => Gen.mapOf(oneOf(generators))
        }
      } yield UserAnswers(
        data = data.foldLeft(Json.obj()) {
          case (obj, (path, value)) =>
            obj.setObject(path.path, value).get
        }
      )
    }
  }
}
