package pages

import models.TaxYear
import pages.behaviours.PageBehaviours

class TaxYearSpec extends PageBehaviours {

  "TaxYearPage" - {

    beRetrievable[TaxYear](TaxYearPage)

    beSettable[TaxYear](TaxYearPage)

    beRemovable[TaxYear](TaxYearPage)
  }
}
