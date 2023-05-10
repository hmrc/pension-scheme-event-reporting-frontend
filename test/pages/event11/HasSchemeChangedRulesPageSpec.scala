package pages.event11

import pages.behaviours.PageBehaviours

class HasSchemeChangedRulesPageSpec extends PageBehaviours {

  "HasSchemeChangedRulesPage" - {

    beRetrievable[Boolean](HasSchemeChangedRulesPage)

    beSettable[Boolean](HasSchemeChangedRulesPage)

    beRemovable[Boolean](HasSchemeChangedRulesPage)
  }
}
