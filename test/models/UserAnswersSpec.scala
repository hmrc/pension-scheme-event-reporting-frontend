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

import base.SpecBase
import data.SampleData
import data.SampleData.{companyDetails, memberDetails, userAnswersWithOneMemberAndEmployer}
import models.enumeration.EventType.Event1
import models.event1.HowAddUnauthPayment.Manual
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import models.event1.{MemberOrEmployerSummary, PaymentDetails}
import org.scalatest.matchers.must.Matchers
import pages.common.MembersDetailsPage
import pages.event1.MembersOrEmployersPage.readsMemberOrEmployerValue
import pages.event1.employer.CompanyDetailsPage
import pages.event1.{HowAddUnauthPaymentPage, MembersOrEmployersPage, PaymentValueAndDatePage, WhoReceivedUnauthPaymentPage}

import java.time.LocalDate

class UserAnswersSpec extends SpecBase with Matchers {



  "getAll" - {
    "must return the list of members or employers" in {
      userAnswersWithOneMemberAndEmployer.getAll(MembersOrEmployersPage)(MembersOrEmployersPage.readsMemberOrEmployer) mustBe
        Seq(MemberOrEmployerSummary(SampleData.memberDetails.fullName, BigDecimal(857.00)),
          MemberOrEmployerSummary(SampleData.companyDetails.companyName, BigDecimal(7687.00)))
    }

    "must return empty list if nothing present" in {
      UserAnswers().getAll(MembersOrEmployersPage)(MembersOrEmployersPage.readsMemberOrEmployer) mustBe Nil
    }

    "must return the list of members or employers where member value and member details missing" in {
      val userAnswersWithOneMemberAndEmployer: UserAnswers = UserAnswers()
        .setOrException(WhoReceivedUnauthPaymentPage(0), Member)
        .setOrException(WhoReceivedUnauthPaymentPage(1), Employer)
        .setOrException(PaymentValueAndDatePage(1), PaymentDetails(BigDecimal(7687.00), LocalDate.of(2022, 11, 9)))
        .setOrException(CompanyDetailsPage(1), companyDetails)
      userAnswersWithOneMemberAndEmployer.getAll(MembersOrEmployersPage)(MembersOrEmployersPage.readsMemberOrEmployer) mustBe
        Seq(MemberOrEmployerSummary("Not entered", BigDecimal(0.00)),
          MemberOrEmployerSummary(SampleData.companyDetails.companyName, BigDecimal(7687.00)))
    }

    "must return the list of members or employers where employer value and employer details missing" in {
      val userAnswersWithOneMemberAndEmployer: UserAnswers = UserAnswers()
        .setOrException(WhoReceivedUnauthPaymentPage(0), Member)
        .setOrException(PaymentValueAndDatePage(0), PaymentDetails(BigDecimal(857.00), LocalDate.of(2022, 11, 9)))
        .setOrException(MembersDetailsPage(Event1, Some(0)), memberDetails)
        .setOrException(WhoReceivedUnauthPaymentPage(1), Employer)
      userAnswersWithOneMemberAndEmployer.getAll(MembersOrEmployersPage)(MembersOrEmployersPage.readsMemberOrEmployer) mustBe
        Seq(MemberOrEmployerSummary(SampleData.memberDetails.fullName, BigDecimal(857.00)),
          MemberOrEmployerSummary("Not entered", BigDecimal(0.00)))
    }
    "must return the list of members or employers if only first question answered" in {
      val userAnswersWithOnlyManualOrUpload: UserAnswers = UserAnswers()
        .setOrException(HowAddUnauthPaymentPage(0), Manual)

      userAnswersWithOnlyManualOrUpload.getAll(MembersOrEmployersPage)(MembersOrEmployersPage.readsMemberOrEmployer) mustBe
        Seq(MemberOrEmployerSummary("Not entered", BigDecimal(0.00)))
    }
  }

  "countAll" - {
    "must count correctly when one member and one employer" in {
      userAnswersWithOneMemberAndEmployer.countAll(MembersOrEmployersPage) mustBe 2
    }

    "must count correctly when nothing present" in {
      UserAnswers().countAll(MembersOrEmployersPage) mustBe 0
    }
  }


  "sumAll" - {
    "must count correctly when one member and one employer" in {
      userAnswersWithOneMemberAndEmployer.sumAll(MembersOrEmployersPage, readsMemberOrEmployerValue) mustBe BigDecimal(8544.00)
    }

    "must count correctly when nothing present" in {
      UserAnswers().sumAll(MembersOrEmployersPage, readsMemberOrEmployerValue) mustBe 0
    }
  }
}
