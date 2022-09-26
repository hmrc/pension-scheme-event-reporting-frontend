package pages.address

import pages.behaviours.PageBehaviours


class ManualAddressPageSpec extends PageBehaviours {

  "ManualAddressPage" - {

    beRetrievable[String](ManualAddressPage)

    beSettable[String](ManualAddressPage)

    beRemovable[String](ManualAddressPage)
  }
}
