package forms.event18

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class RemoveEvent18FormProviderSpec extends BooleanFieldBehaviours {

  private val requiredKey = "removeEvent18.error.required"
  private val invalidKey = "error.boolean"

  val form = new RemoveEvent18FormProvider()()

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
