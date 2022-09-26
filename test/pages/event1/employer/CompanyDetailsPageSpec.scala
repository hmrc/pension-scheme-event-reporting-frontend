package pages.event1.employer

import pages.behaviours.PageBehaviours


class CompanyDetailsPageSpec extends PageBehaviours {

  "CompanyDetailsPage" - {

    beRetrievable[String](CompanyDetailsPage)

    beSettable[String](CompanyDetailsPage)

    beRemovable[String](CompanyDetailsPage)
  }
}
