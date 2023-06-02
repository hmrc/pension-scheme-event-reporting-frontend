package pages.event19

import java.time.LocalDate

import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

class DateChangeMadePageSpec extends PageBehaviours {

  "DateChangeMadePage" - {

    implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
      datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
    }

    beRetrievable[LocalDate](DateChangeMadePage)

    beSettable[LocalDate](DateChangeMadePage)

    beRemovable[LocalDate](DateChangeMadePage)
  }
}
