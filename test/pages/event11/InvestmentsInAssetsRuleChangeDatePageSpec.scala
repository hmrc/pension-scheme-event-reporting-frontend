package pages.event11

import java.time.LocalDate

import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

class InvestmentsInAssetsRuleChangeDatePageSpec extends PageBehaviours {

  "InvestmentsInAssetsRuleChangeDatePage" - {

    implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
      datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
    }

    beRetrievable[LocalDate](InvestmentsInAssetsRuleChangeDatePage)

    beSettable[LocalDate](InvestmentsInAssetsRuleChangeDatePage)

    beRemovable[LocalDate](InvestmentsInAssetsRuleChangeDatePage)
  }
}
