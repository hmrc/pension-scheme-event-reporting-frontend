package pages.event1

import pages.behaviours.PageBehaviours


class UnauthorisedPaymentRecipientNamePageSpec extends PageBehaviours {

  "UnauthorisedPaymentRecipientNamePage" - {

    beRetrievable[String](UnauthorisedPaymentRecipientNamePage)

    beSettable[String](UnauthorisedPaymentRecipientNamePage)

    beRemovable[String](UnauthorisedPaymentRecipientNamePage)
  }
}
