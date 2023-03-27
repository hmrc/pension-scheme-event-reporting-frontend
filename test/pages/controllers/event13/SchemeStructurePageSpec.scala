package pages.controllers.event13

import models.controllers.event13.SchemeStructure
import pages.behaviours.PageBehaviours
import pages.event13.SchemeStructurePage

class SchemeStructureSpec extends PageBehaviours {

  "SchemeStructurePage" - {

    beRetrievable[SchemeStructure](SchemeStructurePage)

    beSettable[SchemeStructure](SchemeStructurePage)

    beRemovable[SchemeStructure](SchemeStructurePage)
  }
}
