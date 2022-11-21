package forms.event18

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class removeEvent23FormProviderSpec extends BooleanFieldBehaviours {

  private val requiredKey = "removeEvent23.error.required"
  private val invalidKey = "error.boolean"

  val form = new removeEvent23FormProvider()()

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
