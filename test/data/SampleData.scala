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

package data

import models.UserAnswers
import models.address.{Address, TolerantAddress}
import models.common.MembersDetails
import models.enumeration.EventType.Event1
import models.event1.PaymentDetails
import models.event1.WhoReceivedUnauthPayment.{Employer, Member}
import models.event1.employer.{CompanyDetails, LoanDetails}
import models.event1.member.SchemeDetails
import pages.common.MembersDetailsPage
import pages.event1.{PaymentValueAndDatePage, WhoReceivedUnauthPaymentPage}
import pages.event1.employer.CompanyDetailsPage
import utils.{CountryOptions, InputOption}

import java.time.LocalDate

object SampleData {
  private val options: Seq[InputOption] = Seq(
    InputOption("territory:AE-AZ", "Abu Dhabi"), InputOption("country:AF", "Afghanistan"),
    InputOption("GB", "Great Britain")
  )

  def countryOptions: CountryOptions = new CountryOptions(options)

  val seqAddresses: Seq[Address] = Seq[Address](
    Address(
      addressLine1 = "addr11",
      addressLine2 = "addr12",
      addressLine3 = Some("addr13"),
      addressLine4 = Some("addr14"),
      postcode = Some("zz11zz"),
      country = "GB"
    ),
    Address(
      addressLine1 = "addr21",
      addressLine2 = "addr22",
      addressLine3 = Some("addr23"),
      addressLine4 = Some("addr24"),
      postcode = Some("zz11zz"),
      country = "GB"
    )
  )

  val employerAddress: Address = Address(
    addressLine1 = "addr11",
    addressLine2 = "addr12",
    addressLine3 = Some("addr13"),
    addressLine4 = Some("addr14"),
    postcode = Some("zz11zz"),
    country = "GB"
  )

  val seqTolerantAddresses: Seq[TolerantAddress] = Seq[TolerantAddress](
    TolerantAddress(
      addressLine1 = Some("addr11"),
      addressLine2 = Some("addr12"),
      addressLine3 = Some("addr13"),
      addressLine4 = Some("addr14"),
      postcode = Some("zz11zz"),
      countryOpt = Some("GB")
    ),
    TolerantAddress(
      addressLine1 = Some("addr21"),
      addressLine2 = Some("addr22"),
      addressLine3 = Some("addr23"),
      addressLine4 = Some("addr24"),
      postcode = Some("zz11zz"),
      countryOpt = Some("GB")
    )
  )
  val companyDetails: CompanyDetails = CompanyDetails("Company Name", "12345678")

  val memberDetails: MembersDetails = MembersDetails("Joe", "Bloggs", "AA234567V")

  def booleanCYAVal(value: Boolean) = if (value) "site.yes" else "site.no"

  val loanDetails: LoanDetails = LoanDetails(Some(BigDecimal(10.00)), Some(BigDecimal(20.57)))

  val schemeDetails: SchemeDetails = SchemeDetails(Some("SchemeName"), Some("SchemeReference"))

  val userAnswersWithOneMemberAndEmployer: UserAnswers = UserAnswers()
    .setOrException(WhoReceivedUnauthPaymentPage(0), Member)
    .setOrException(PaymentValueAndDatePage(0), PaymentDetails(BigDecimal(857.00), LocalDate.of(2022, 11, 9)))
    .setOrException(MembersDetailsPage(Event1, Some(0)), memberDetails)
    .setOrException(WhoReceivedUnauthPaymentPage(1), Employer)
    .setOrException(PaymentValueAndDatePage(1), PaymentDetails(BigDecimal(7687.00), LocalDate.of(2022, 11, 9)))
    .setOrException(CompanyDetailsPage(1), companyDetails)

}
