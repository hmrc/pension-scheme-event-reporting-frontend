package pages

import pages.behaviours.PageBehaviours


class DeclarationPspPageSpec extends PageBehaviours {

  "DeclarationPspPage" - {

    beRetrievable[String](DeclarationPspPage)

    beSettable[String](DeclarationPspPage)

    beRemovable[String](DeclarationPspPage)
  }
}
