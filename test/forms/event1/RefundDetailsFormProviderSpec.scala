package forms.event1

import forms.behaviours.OptionFieldBehaviours
import models.event1.RefundDetails
import play.api.data.FormError

class RefundDetailsFormProviderSpec extends OptionFieldBehaviours {

  private val form = new RefundDetailsFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "refundDetails.error.required"

    behave like optionsField[RefundDetails](
      form,
      fieldName,
      validValues  = RefundDetails.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
