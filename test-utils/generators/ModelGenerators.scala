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

package generators

import models._
import models.common.{ChooseTaxYear, MembersDetails}
import models.event1.employer.{CompanyDetails, LoanDetails}
import models.event1.member.{ReasonForTheOverpaymentOrWriteOff, RefundOfContributions, SchemeDetails, WhoWasTheTransferMade}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.domain.Nino

import scala.math.BigDecimal.RoundingMode

trait ModelGenerators {

  implicit lazy val arbitraryWhatChange: Arbitrary[event20.WhatChange] =
    Arbitrary {
      Gen.oneOf(event20.WhatChange.values.toSeq)
    }

  implicit lazy val arbitraryFileUploadResult: Arbitrary[fileUpload.FileUploadResult] =
    Arbitrary {
      Gen.oneOf(fileUpload.FileUploadResult.values.toSeq)
    }

  implicit lazy val arbitraryBecomeOrCeaseScheme: Arbitrary[event10.BecomeOrCeaseScheme] =
    Arbitrary {
      Gen.oneOf(event10.BecomeOrCeaseScheme.values.toSeq)
    }

  implicit lazy val arbitraryHowManySchemeMembers: Arbitrary[event14.HowManySchemeMembers] =
    Arbitrary {
      Gen.oneOf(event14.HowManySchemeMembers.values.toSeq)
    }

  implicit lazy val arbitraryReasonForBenefits: Arbitrary[event3.ReasonForBenefits] =
    Arbitrary {
      Gen.oneOf(event3.ReasonForBenefits.values.toSeq)
    }

  implicit lazy val arbitraryPaymentType: Arbitrary[event8a.PaymentType] =
    Arbitrary {
      Gen.oneOf(event8a.PaymentType.values.toSeq)
    }

  implicit lazy val arbitraryTypeOfProtection: Arbitrary[event8.TypeOfProtection] =
    Arbitrary {
      Gen.oneOf(event8.TypeOfProtection.values.toSeq)
    }

  implicit lazy val arbitrarySchemeStructure: Arbitrary[event13.SchemeStructure] =
    Arbitrary {
      Gen.oneOf(event13.SchemeStructure.values.toSeq)
    }

  implicit lazy val arbitraryTaxYear: Arbitrary[TaxYear] =
    Arbitrary {
      Gen.oneOf(TaxYear.values.toSeq)
    }

  implicit lazy val arbitraryManualOrUpload: Arbitrary[common.ManualOrUpload] =
    Arbitrary {
      Gen.oneOf(common.ManualOrUpload.values)
    }

  implicit lazy val arbitraryChooseTaxYear: Arbitrary[ChooseTaxYear] =
    Arbitrary {
      Gen.oneOf(common.ChooseTaxYear.values(2021))
    }

  implicit lazy val arbitraryReasonForTheOverpaymentOrWriteOff: Arbitrary[ReasonForTheOverpaymentOrWriteOff] =
    Arbitrary {
      Gen.oneOf(ReasonForTheOverpaymentOrWriteOff.values)
    }

  implicit lazy val arbitraryRefundOfContributions: Arbitrary[RefundOfContributions] =
    Arbitrary {
      Gen.oneOf(event1.member.RefundOfContributions.values)
    }

  implicit lazy val arbitraryWhoWasTheTransferMade: Arbitrary[WhoWasTheTransferMade] =
    Arbitrary {
      Gen.oneOf(event1.member.WhoWasTheTransferMade.values)
    }

  implicit lazy val arbitraryEmployerPaymentNature: Arbitrary[event1.employer.PaymentNature] =
    Arbitrary {
      Gen.oneOf(event1.employer.PaymentNature.values)
    }

  implicit lazy val arbitraryNino: Arbitrary[Nino] = Arbitrary {
    for {
      firstChar <- Gen.oneOf('A', 'C', 'E', 'H', 'J', 'L', 'M', 'O', 'P', 'R', 'S', 'W', 'X', 'Y').map(_.toString)
      secondChar <- Gen.oneOf('A', 'B', 'C', 'E', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'R', 'S', 'T', 'W', 'X', 'Y', 'Z').map(_.toString)
      digits <- Gen.listOfN(6, Gen.numChar)
      lastChar <- Gen.oneOf('A', 'B', 'C', 'D')
    } yield Nino(firstChar ++ secondChar ++ digits.mkString :+ lastChar)
  }

  implicit lazy val arbitraryWhoReceivedUnauthPayment: Arbitrary[event1.WhoReceivedUnauthPayment] =
    Arbitrary {
      Gen.oneOf(event1.WhoReceivedUnauthPayment.values)
    }

  implicit lazy val arbitraryMembersDetails: Arbitrary[MembersDetails] =
    Arbitrary {
      val list = for {
        firstName <- Seq("validFirstName1", "validFirstName2")
        lastName <- Seq("validLastName1", "validLastName2")
        nino <- arbitrary[Nino].sample
      } yield MembersDetails(firstName, lastName, nino.nino)

      Gen.oneOf(list)
    }

  implicit lazy val arbitraryLoanDetails: Arbitrary[LoanDetails] =
    Arbitrary {
      for {
        loanAmount <- arbitrary[BigDecimal].map(_.setScale(2, RoundingMode.FLOOR))
        fundValue <- arbitrary[BigDecimal].map(_.setScale(2, RoundingMode.FLOOR))
      } yield LoanDetails(loanAmount, fundValue)
    }

  implicit lazy val arbitrarySchemeDetails: Arbitrary[SchemeDetails] =
    Arbitrary {
      val list = for {
        schemeName <- Seq("validSchemeName1", "validSchemeName2")
        reference <- Seq("validReferenceNumber1", "validReferenceNumber2")
      } yield SchemeDetails(Some(schemeName), Some(reference))

      Gen.oneOf(list)
    }

  implicit lazy val arbitraryCompanyDetails: Arbitrary[CompanyDetails] =
    Arbitrary {
      val list = for {
        companyName <- Seq("validCompanyName1", "validCompanyName2")
        companyNumber <- Seq("validCompanyNumber1", "validCompanyNumber2")
      } yield CompanyDetails(companyName, companyNumber)

      Gen.oneOf(list)
    }

  implicit lazy val arbitraryPaymentNature: Arbitrary[event1.PaymentNature] =
    Arbitrary {
      Gen.oneOf(event1.PaymentNature.values)
    }

  implicit lazy val arbitraryEventSelection: Arbitrary[EventSelection] =
    Arbitrary {
      Gen.oneOf(EventSelection.values)
    }
}
