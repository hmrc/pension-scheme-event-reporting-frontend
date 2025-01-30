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

package models.requests

import data.SampleData.emptyUserAnswersWithTaxYear
import models.enumeration.AdministratorOrPractitioner.Administrator
import models.enumeration.VersionStatus.Submitted
import models.{EROverview, EROverviewVersion, LoggedInUser, TaxYear, VersionInfo}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{EventReportingOverviewPage, VersionInfoPage}
import play.api.mvc.Call
import play.api.test.FakeRequest

import java.time.LocalDate

class DataRequestSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "DataRequest" - {

    val erOverviewSeq = Seq(EROverview(
      LocalDate.of(2022, 4, 6),
      LocalDate.of(2023, 4, 5),
      TaxYear("2022"),
      tpssReportPresent = true,
      Some(EROverviewVersion(
        3,
        submittedVersionAvailable = true,
        compiledVersionAvailable = false
      ))
    ),
      EROverview(
      LocalDate.of(2023, 4, 6),
      LocalDate.of(2024, 4, 5),
      TaxYear("2023"),
      tpssReportPresent = true,
      Some(EROverviewVersion(
        2,
        submittedVersionAvailable = true,
        compiledVersionAvailable = false
      ))
    ))

    "must return read only if version selected is less than current report version" in {

      val uA = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(1, Submitted))
      .setOrException(EventReportingOverviewPage, erOverviewSeq)

      val dataReqObj = DataRequest("pstr", "schemeName", "url", FakeRequest(Call("GET", "/")), LoggedInUser("", Administrator, ""), uA, "S2400000041")

      dataReqObj.readOnly mustBe true
    }

    "must return false if version selected is equal to current report version" in {

      val uA = emptyUserAnswersWithTaxYear.setOrException(VersionInfoPage, VersionInfo(3, Submitted))
      .setOrException(EventReportingOverviewPage, erOverviewSeq)

      val dataReqObj = DataRequest("pstr", "schemeName", "url", FakeRequest(Call("GET", "/")), LoggedInUser("", Administrator, ""), uA, "S2400000041")

      dataReqObj.readOnly mustBe false
    }

  }
}
