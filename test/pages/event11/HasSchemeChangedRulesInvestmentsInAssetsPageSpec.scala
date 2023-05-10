package pages.event11

import pages.behaviours.PageBehaviours

class HasSchemeChangedRulesInvestmentsInAssetsPageSpec extends PageBehaviours {

  "HasSchemeChangedRulesInvestmentsInAssetsPage" - {

    beRetrievable[Boolean](HasSchemeChangedRulesInvestmentsInAssetsPage)

    beSettable[Boolean](HasSchemeChangedRulesInvestmentsInAssetsPage)

    beRemovable[Boolean](HasSchemeChangedRulesInvestmentsInAssetsPage)
  }
}
