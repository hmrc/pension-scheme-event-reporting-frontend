package forms$if(package.empty)$$else$.$package$$endif$

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends BooleanFieldBehaviours {

  private val requiredKey = "$className;format="decap"$.error.required"
  private val invalidKey = "error.boolean"

  val form = new $className$FormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
