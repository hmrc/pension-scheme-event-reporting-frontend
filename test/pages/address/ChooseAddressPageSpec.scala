package pages.address

import models.address.ChooseAddress
import pages.behaviours.PageBehaviours

class ChooseAddressSpec extends PageBehaviours {

  "ChooseAddressPage" - {

    beRetrievable[ChooseAddress](ChooseAddressPage)

    beSettable[ChooseAddress](ChooseAddressPage)

    beRemovable[ChooseAddress](ChooseAddressPage)
  }
}
