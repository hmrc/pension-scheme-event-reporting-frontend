package forms.event11

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class HasSchemeChangedRulesFormProviderSpec extends BooleanFieldBehaviours {

  private val requiredKey = "hasSchemeChangedRules.error.required"
  private val invalidKey = "error.boolean"

  val form = new HasSchemeChangedRulesFormProvider()()

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
