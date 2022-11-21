package pages.event18

import pages.behaviours.PageBehaviours

class removeEvent23PageSpec extends PageBehaviours {

  "removeEvent23Page" - {

    beRetrievable[Boolean](removeEvent23Page)

    beSettable[Boolean](removeEvent23Page)

    beRemovable[Boolean](removeEvent23Page)
  }
}
