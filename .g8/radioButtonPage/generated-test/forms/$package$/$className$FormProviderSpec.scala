package forms$if(package.empty)$$else$.$package$$endif$

import forms.behaviours.OptionFieldBehaviours
import models$if(!package.empty)$.$package$$endif$.$className$
import play.api.data.FormError

class $className$FormProviderSpec extends OptionFieldBehaviours {

  val form = new $className$FormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "$className;format="decap"$.error.required"

    behave like optionsField[$className$](
      form,
      fieldName,
      validValues  = $className$.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
