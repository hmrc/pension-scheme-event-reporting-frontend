package pages

import models.TestCheckBox
import pages.behaviours.PageBehaviours

class TestCheckBoxPageSpec extends PageBehaviours {

  "TestCheckBoxPage" - {

    beRetrievable[Set[TestCheckBox]](TestCheckBoxPage)

    beSettable[Set[TestCheckBox]](TestCheckBoxPage)

    beRemovable[Set[TestCheckBox]](TestCheckBoxPage)
  }
}
