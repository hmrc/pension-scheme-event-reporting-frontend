package pages.event1

import pages.behaviours.PageBehaviours

class PaymentValueAndDatePageSpec extends PageBehaviours {

  "PaymentValueAndDatePage" - {

    beRetrievable[Int](PaymentValueAndDatePage)

    beSettable[Int](PaymentValueAndDatePage)

    beRemovable[Int](PaymentValueAndDatePage)
  }
}
