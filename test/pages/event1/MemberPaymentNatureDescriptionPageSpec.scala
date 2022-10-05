package pages.event1

import pages.behaviours.PageBehaviours


class MemberPaymentNatureDescriptionPageSpec extends PageBehaviours {

  "MemberPaymentNatureDescriptionPage" - {

    beRetrievable[String](MemberPaymentNatureDescriptionPage)

    beSettable[String](MemberPaymentNatureDescriptionPage)

    beRemovable[String](MemberPaymentNatureDescriptionPage)
  }
}
