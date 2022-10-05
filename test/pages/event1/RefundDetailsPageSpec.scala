package pages.event1

import models.event1.RefundDetails
import pages.behaviours.PageBehaviours

class RefundDetailsSpec extends PageBehaviours {

  "RefundDetailsPage" - {

    beRetrievable[RefundDetails](RefundDetailsPage)

    beSettable[RefundDetails](RefundDetailsPage)

    beRemovable[RefundDetails](RefundDetailsPage)
  }
}
