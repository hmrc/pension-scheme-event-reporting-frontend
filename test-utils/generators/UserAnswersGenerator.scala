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

package generators

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import pages.common.MembersDetailsPage
import pages.event1.employer.{EmployerPaymentNatureDescriptionPage, EmployerTangibleMoveablePropertyPage, UnauthorisedPaymentRecipientNamePage}
import pages.event1.member._
import pages.event13.SchemeStructurePage
import pages.event3.{EarlyBenefitsBriefDescriptionPage, ReasonForBenefitsPage}
import pages.eventWindUp.SchemeWindUpDatePage
import play.api.libs.json.{JsValue, Json}

trait UserAnswersGenerator extends TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[_], JsValue)]] = {
    arbitrary[(event14.HowManySchemeMembersPage.type, JsValue)] ::
    arbitrary[(event12.DateOfChangePage.type, JsValue)] ::
      arbitrary[(event12.HasSchemeChangedRulesPage.type, JsValue)] ::
      arbitrary[(event10.ContractsOrPoliciesPage.type, JsValue)] ::
      arbitrary[(event10.SchemeChangeDatePage.type, JsValue)] ::
      arbitrary[(event10.BecomeOrCeaseSchemePage.type, JsValue)] ::
      arbitrary[(event2.DatePaidPage, JsValue)] ::
      arbitrary[(event2.AmountPaidPage, JsValue)] ::
      arbitrary[(EarlyBenefitsBriefDescriptionPage, JsValue)] ::
      arbitrary[(ReasonForBenefitsPage, JsValue)] ::
      arbitrary[(event13.SchemeStructureDescriptionPage.type, JsValue)] ::
      arbitrary[(event13.ChangeDatePage.type, JsValue)] ::
      arbitrary[(SchemeStructurePage.type, JsValue)] ::
      arbitrary[(TaxYearPage.type, JsValue)] ::
      arbitrary[(event18.RemoveEvent18Page.type, JsValue)] ::
      arbitrary[(event1.PaymentValueAndDatePage, JsValue)] ::
      arbitrary[(event1.member.ErrorDescriptionPage, JsValue)] ::
      arbitrary[(BenefitsPaidEarlyPage, JsValue)] ::
      arbitrary[(event1.employer.PaymentNaturePage, JsValue)] ::
      arbitrary[(event1.employer.CompanyDetailsPage, JsValue)] ::
      arbitrary[(BenefitInKindBriefDescriptionPage, JsValue)] ::
      arbitrary[(event1.SchemeUnAuthPaySurchargeMemberPage, JsValue)] ::
      arbitrary[(event1.ValueOfUnauthorisedPaymentPage, JsValue)] ::
      arbitrary[(event1.DoYouHoldSignedMandatePage, JsValue)] ::
      arbitrary[(MembersDetailsPage, JsValue)] ::
      arbitrary[(PaymentNaturePage, JsValue)] ::
      arbitrary[(SchemeWindUpDatePage.type, JsValue)] ::
      arbitrary[(event18.Event18ConfirmationPage.type, JsValue)] ::
      arbitrary[(EventSummaryPage.type, JsValue)] ::
      arbitrary[(EventSelectionPage.type, JsValue)] ::
      arbitrary[(event1.member.SchemeDetailsPage, JsValue)] ::
      arbitrary[(event1.member.WhoWasTheTransferMadePage, JsValue)] ::
      arbitrary[(event1.member.UnauthorisedPaymentRecipientNamePage, JsValue)] ::
      arbitrary[(event1.employer.LoanDetailsPage, JsValue)] ::
      arbitrary[(RefundOfContributionsPage, JsValue)] ::
      arbitrary[(event1.member.UnauthorisedPaymentRecipientNamePage, JsValue)] ::
      arbitrary[(RefundOfContributionsPage, JsValue)] ::
      arbitrary[(EmployerPaymentNatureDescriptionPage, JsValue)] ::
      arbitrary[(MemberPaymentNatureDescriptionPage, JsValue)] ::
      arbitrary[(EmployerTangibleMoveablePropertyPage, JsValue)] ::
      arbitrary[(MemberTangibleMoveablePropertyPage, JsValue)] ::
      arbitrary[(ReasonForTheOverpaymentOrWriteOffPage, JsValue)] ::
      arbitrary[(UnauthorisedPaymentRecipientNamePage, JsValue)] ::
      Nil
  }

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
