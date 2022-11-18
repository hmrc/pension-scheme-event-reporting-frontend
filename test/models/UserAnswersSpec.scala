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
import data.SampleData._
import models.common.MembersSummary
import models.common.MembersSummary.readsMemberValue
import models.enumeration.EventType.{Event1, Event22}
import models.event1.HowAddUnauthPayment.Manual
import models.event1.MembersOrEmployersSummary.readsMemberOrEmployerValue
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import models.event1.{MembersOrEmployersSummary, PaymentDetails}
import org.scalatest.matchers.must.Matchers
import pages.common.{ChooseTaxYearPage, MembersDetailsPage, MembersOrEmployersPage, MembersPage}
import pages.event1.employer.CompanyDetailsPage
import pages.event1.{HowAddUnauthPaymentPage, PaymentValueAndDatePage, WhoReceivedUnauthPaymentPage}

import java.time.LocalDate

class UserAnswersSpec extends SpecBase with Matchers {



  "getAll" - {
    "must return the list of members or employers" in {
      userAnswersWithOneMemberAndEmployer.getAll(MembersOrEmployersPage(Event1))(MembersOrEmployersSummary.readsMemberOrEmployer(Event1)) mustBe
        Seq(MembersOrEmployersSummary(SampleData.memberDetails.fullName, BigDecimal(857.00)),
          MembersOrEmployersSummary(SampleData.companyDetails.companyName, BigDecimal(7687.00)))
    }

    "must return empty list if nothing present" in {
      UserAnswers().getAll(MembersOrEmployersPage(Event1))(MembersOrEmployersSummary.readsMemberOrEmployer(Event1)) mustBe Nil
    }

    "must return the list of members or employers where member value and member details missing" in {
      val userAnswersWithOneMemberAndEmployer: UserAnswers = UserAnswers()
        .setOrException(WhoReceivedUnauthPaymentPage(0), Member)
        .setOrException(WhoReceivedUnauthPaymentPage(1), Employer)
        .setOrException(PaymentValueAndDatePage(1), PaymentDetails(BigDecimal(7687.00), LocalDate.of(2022, 11, 9)))
        .setOrException(CompanyDetailsPage(1), companyDetails)
      userAnswersWithOneMemberAndEmployer.getAll(MembersOrEmployersPage(Event1))(MembersOrEmployersSummary.readsMemberOrEmployer(Event1)) mustBe
        Seq(MembersOrEmployersSummary("Not entered", BigDecimal(0.00)),
          MembersOrEmployersSummary(SampleData.companyDetails.companyName, BigDecimal(7687.00)))
    }

    "must return the list of members or employers where employer value and employer details missing" in {
      val userAnswersWithOneMemberAndEmployer: UserAnswers = UserAnswers()
        .setOrException(WhoReceivedUnauthPaymentPage(0), Member)
        .setOrException(PaymentValueAndDatePage(0), PaymentDetails(BigDecimal(857.00), LocalDate.of(2022, 11, 9)))
        .setOrException(MembersDetailsPage(Event1, 0), memberDetails)
        .setOrException(WhoReceivedUnauthPaymentPage(1), Employer)
      userAnswersWithOneMemberAndEmployer.getAll(MembersOrEmployersPage(Event1))(MembersOrEmployersSummary.readsMemberOrEmployer(Event1)) mustBe
        Seq(MembersOrEmployersSummary(SampleData.memberDetails.fullName, BigDecimal(857.00)),
          MembersOrEmployersSummary("Not entered", BigDecimal(0.00)))
    }
    "must return the list of members or employers if only first question answered" in {
      val userAnswersWithOnlyManualOrUpload: UserAnswers = UserAnswers()
        .setOrException(HowAddUnauthPaymentPage(0), Manual)

      userAnswersWithOnlyManualOrUpload.getAll(MembersOrEmployersPage(Event1))(MembersOrEmployersSummary.readsMemberOrEmployer(Event1)) mustBe
        Seq(MembersOrEmployersSummary("Not entered", BigDecimal(0.00)))
    }
  }

  "countAll" - {
    "must count correctly when one member and one employer" in {
      userAnswersWithOneMemberAndEmployer.countAll(MembersOrEmployersPage(Event1)) mustBe 2
    }

    "must count correctly when nothing present" in {
      UserAnswers().countAll(MembersOrEmployersPage(Event1)) mustBe 0
    }
  }


  "sumAll" - {
    "must count correctly when one member and one employer" in {
      userAnswersWithOneMemberAndEmployer.sumAll(MembersOrEmployersPage(Event1), readsMemberOrEmployerValue) mustBe BigDecimal(8544.00)
    }

    "must count correctly when nothing present" in {
      UserAnswers().sumAll(MembersOrEmployersPage(Event1), readsMemberOrEmployerValue) mustBe 0
    }
  }
  "event22" - {
    "getAll" - {
      "must return the list of members or employers" in {
        sampleMemberJourneyDataEvent22.getAll(MembersPage(Event22))(MembersSummary.readsMember) mustBe
          Seq(MembersSummary(SampleData.memberDetails.fullName, BigDecimal(14.99), SampleData.memberDetails.nino))
      }

      "must return empty list if nothing present" in {
        UserAnswers().getAll(MembersPage(Event22))(MembersSummary.readsMember) mustBe Nil
      }

      "must return the list of members or employers where member value and member details missing" in {
        val userAnswersWithOneMember: UserAnswers = UserAnswers()
          .setOrException(ChooseTaxYearPage(Event22, 0), taxYear)

        userAnswersWithOneMember.getAll(MembersPage(Event22))(MembersSummary.readsMember) mustBe
          Seq(MembersSummary("Not entered", BigDecimal(0.00), "Not entered"))
      }

    }

    "countAll" - {
      "must count correctly when two members are present" in {
        sampleTwoMemberJourneyData(Event22).countAll(MembersPage(Event22)) mustBe 2
      }

      "must count correctly when nothing present" in {
        UserAnswers().countAll(MembersPage(Event22)) mustBe 0
      }
    }


    "sumAll" - {
      "must count correctly when two members are present" in {
        sampleTwoMemberJourneyData(Event22).sumAll(MembersPage(Event22), readsMemberValue) mustBe BigDecimal(29.98)
      }

      "must count correctly when nothing present" in {
        UserAnswers().sumAll(MembersPage(Event22), readsMemberValue) mustBe 0
      }
    }
  }
}
