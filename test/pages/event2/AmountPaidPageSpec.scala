package pages.event2

import pages.behaviours.PageBehaviours

class AmountPaidPageSpec extends PageBehaviours {

  "AmountPaidPage" - {

    beRetrievable[Int](AmountPaidPage)

    beSettable[Int](AmountPaidPage)

    beRemovable[Int](AmountPaidPage)
  }
}
