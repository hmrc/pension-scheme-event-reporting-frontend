package forms

import forms.behaviours.OptionFieldBehaviours
import models.TaxYear
import play.api.data.FormError

class TaxYearFormProviderSpec extends OptionFieldBehaviours {

  private val form = new TaxYearFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "taxYear.error.required"

    behave like optionsField[TaxYear](
      form,
      fieldName,
      validValues  = TaxYear.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
