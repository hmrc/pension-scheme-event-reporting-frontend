package forms.event10

import forms.behaviours.OptionFieldBehaviours
import models.event10.BecomeOrCeaseScheme
import play.api.data.FormError

class BecomeOrCeaseSchemeFormProviderSpec extends OptionFieldBehaviours {

  private val form = new BecomeOrCeaseSchemeFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "becomeOrCeaseScheme.error.required"

    behave like optionsField[BecomeOrCeaseScheme](
      form,
      fieldName,
      validValues  = BecomeOrCeaseScheme.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
