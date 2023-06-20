package pages.event20A

import pages.behaviours.PageBehaviours


class Event20APspDeclarationPageSpec extends PageBehaviours {

  "Event20APspDeclarationPage" - {

    beRetrievable[String](Event20APspDeclarationPage)

    beSettable[String](Event20APspDeclarationPage)

    beRemovable[String](Event20APspDeclarationPage)
  }
}
