package pages.common

import pages.behaviours.PageBehaviours

class RemoveMemberPageSpec extends PageBehaviours {

  "RemoveMemberPage" - {

    beRetrievable[Boolean](RemoveMemberPage)

    beSettable[Boolean](RemoveMemberPage)

    beRemovable[Boolean](RemoveMemberPage)
  }
}
