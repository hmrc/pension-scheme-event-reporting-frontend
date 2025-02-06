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

package models.request

import base.SpecBase
import models.enumeration.AdministratorOrPractitioner.Administrator
import models.enumeration.VersionStatus.{Compiled, Submitted}
import models.requests.DataRequest
import models.{LoggedInUser, UserAnswers, VersionInfo}
import pages.VersionInfoPage
import play.api.mvc.Call
import play.api.test.FakeRequest

class DataRequestSpec extends SpecBase {


  "isReportSubmitted" - {

    "must return true when getVersionInfo status is Submitted" in {

      val request = FakeRequest.apply(Call("GET", "/"))
      val ua = UserAnswers().setOrException(VersionInfoPage, VersionInfo(1, Submitted), nonEventTypeData = true)

      val dataRequest = DataRequest("", "", "", request, LoggedInUser("user", Administrator, "psaId"), ua, "srn")

      dataRequest.isReportSubmitted mustBe true
    }

    "must return false when getVersionInfo status is Compiled" in {

      val request = FakeRequest.apply(Call("GET", "/"))
      val ua = UserAnswers().setOrException(VersionInfoPage, VersionInfo(1, Compiled), nonEventTypeData = true)

      val dataRequest = DataRequest("", "", "", request, LoggedInUser("user", Administrator, "psaId"), ua, "srn")

      dataRequest.isReportSubmitted mustBe false
    }

    "must return false when there is no version info " in {

      val request = FakeRequest.apply(Call("GET", "/"))
      val ua = UserAnswers()

      val dataRequest = DataRequest("", "", "", request, LoggedInUser("user", Administrator, "psaId"), ua, "srn")

      dataRequest.isReportSubmitted mustBe false
    }

  }

}
