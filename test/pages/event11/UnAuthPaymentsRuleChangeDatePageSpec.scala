package pages.event11

import java.time.LocalDate

import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

class UnAuthPaymentsRuleChangeDatePageSpec extends PageBehaviours {

  "UnAuthPaymentsRuleChangeDatePage" - {

    implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
      datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
    }

    beRetrievable[LocalDate](UnAuthPaymentsRuleChangeDatePage)

    beSettable[LocalDate](UnAuthPaymentsRuleChangeDatePage)

    beRemovable[LocalDate](UnAuthPaymentsRuleChangeDatePage)
  }
}
