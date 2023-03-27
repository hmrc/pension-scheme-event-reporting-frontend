package pages.event13

import pages.behaviours.PageBehaviours


class SchemeStructureDescriptionPageSpec extends PageBehaviours {

  "SchemeStructureDescriptionPage" - {

    beRetrievable[String](SchemeStructureDescriptionPage)

    beSettable[String](SchemeStructureDescriptionPage)

    beRemovable[String](SchemeStructureDescriptionPage)
  }
}
