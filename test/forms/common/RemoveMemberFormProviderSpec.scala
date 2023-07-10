package forms.common

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class RemoveMemberFormProviderSpec extends BooleanFieldBehaviours {

  private val requiredKey = "removeMember.error.required"
  private val invalidKey = "error.boolean"

  val form = new RemoveMemberFormProvider()()

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
