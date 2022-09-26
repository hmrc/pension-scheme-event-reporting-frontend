package pages.address

import pages.behaviours.PageBehaviours


class EnterPostcodePageSpec extends PageBehaviours {

  "EnterPostcodePage" - {

    beRetrievable[String](EnterPostcodePage)

    beSettable[String](EnterPostcodePage)

    beRemovable[String](EnterPostcodePage)
  }
}
