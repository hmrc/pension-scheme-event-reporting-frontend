package pages.event13

import java.time.LocalDate

import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

class ChangeDatePageSpec extends PageBehaviours {

  "ChangeDatePage" - {

    implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
      datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
    }

    beRetrievable[LocalDate](ChangeDatePage)

    beSettable[LocalDate](ChangeDatePage)

    beRemovable[LocalDate](ChangeDatePage)
  }
}
