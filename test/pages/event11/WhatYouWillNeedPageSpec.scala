package pages.event11

import pages.behaviours.PageBehaviours


class WhatYouWillNeedPageSpec extends PageBehaviours {

  "WhatYouWillNeedPage" - {

    beRetrievable[String](WhatYouWillNeedPage)

    beSettable[String](WhatYouWillNeedPage)

    beRemovable[String](WhatYouWillNeedPage)
  }
}
