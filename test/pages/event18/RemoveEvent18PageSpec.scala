package pages.event18

import pages.behaviours.PageBehaviours

class RemoveEvent18PageSpec extends PageBehaviours {

  "RemoveEvent18Page" - {

    beRetrievable[Boolean](RemoveEvent18Page)

    beSettable[Boolean](RemoveEvent18Page)

    beRemovable[Boolean](RemoveEvent18Page)
  }
}
