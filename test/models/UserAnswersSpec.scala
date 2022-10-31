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

import data.SampleData
import data.SampleData.userAnswersWithOneMemberAndEmployer
import models.event1.MemberOrEmployerSummary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class UserAnswersSpec extends AnyFreeSpec with Matchers {

  "memberOrEmployerSummaryEvent1" - {

    "must return the list of members or employers" in {
      userAnswersWithOneMemberAndEmployer.memberOrEmployerSummaryEvent1 mustBe
        Seq(MemberOrEmployerSummary(SampleData.memberDetails.fullName, BigDecimal(857.00), 0),
          MemberOrEmployerSummary(SampleData.companyDetails.companyName, BigDecimal(7687.00), 1))
    }

  }
}
