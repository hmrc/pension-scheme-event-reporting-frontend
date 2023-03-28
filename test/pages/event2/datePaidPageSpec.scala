package pages.event2

import java.time.LocalDate

import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

class datePaidPageSpec extends PageBehaviours {

  "DatePaidPage" - {

    implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
      datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
    }

    beRetrievable[LocalDate](DatePaidPage)

    beSettable[LocalDate](DatePaidPage)

    beRemovable[LocalDate](DatePaidPage)
  }
}
