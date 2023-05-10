package forms.event11

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class HasSchemeChangedRulesInvestmentsInAssetsFormProviderSpec extends BooleanFieldBehaviours {

  //LDS ignore
  private val requiredKey = "hasSchemeChangedRulesInvestmentsInAssets.error.required"
  private val invalidKey = "error.boolean"

  val form = new HasSchemeChangedRulesInvestmentsInAssetsFormProvider()()

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
