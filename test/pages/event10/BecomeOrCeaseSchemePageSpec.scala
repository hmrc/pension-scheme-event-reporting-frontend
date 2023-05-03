package pages.event10

import models.event10.BecomeOrCeaseScheme
import pages.behaviours.PageBehaviours

class BecomeOrCeaseSchemeSpec extends PageBehaviours {

  "BecomeOrCeaseSchemePage" - {

    beRetrievable[BecomeOrCeaseScheme](BecomeOrCeaseSchemePage)

    beSettable[BecomeOrCeaseScheme](BecomeOrCeaseSchemePage)

    beRemovable[BecomeOrCeaseScheme](BecomeOrCeaseSchemePage)
  }
}
