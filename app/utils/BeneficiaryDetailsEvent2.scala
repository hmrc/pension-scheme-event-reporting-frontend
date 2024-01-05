/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import models.common.MembersDetails
import models.enumeration.EventType.Event2
import models.{Index, UserAnswers}
import pages.common.MembersDetailsPage

object BeneficiaryDetailsEvent2 {

  def getBeneficiaryName(userAnswers: Option[UserAnswers], index: Index): String = {
    val beneficiaryFullNameOpt = userAnswers.flatMap {
      _.get(MembersDetailsPage(Event2, index, Event2MemberPageNumbers.SECOND_PAGE_BENEFICIARY)).map {
        g => MembersDetails(g.firstName, g.lastName, g.nino).fullName
      }
    }
    beneficiaryFullNameOpt match {
      case Some(beneficiaryName) => beneficiaryName
      case _ => throw new RuntimeException("Beneficiary name does not exist")
    }
  }
}
