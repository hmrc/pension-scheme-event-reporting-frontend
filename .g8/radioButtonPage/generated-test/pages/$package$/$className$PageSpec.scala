package pages$if(package.empty)$$else$.$package$$endif$

import models$if(!package.empty)$.$package$$endif$.$className$
import pages.behaviours.PageBehaviours

class $className$Spec extends PageBehaviours {

  "$className$Page" - {

    beRetrievable[$className$]($className$Page)

    beSettable[$className$]($className$Page)

    beRemovable[$className$]($className$Page)
  }
}
