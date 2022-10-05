package forms.event1

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class UnauthorisedPaymentRecipientNameFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "unauthorisedPaymentRecipientName.error.required"
  private val lengthKey = "unauthorisedPaymentRecipientName.error.length"
  private val maxLength = 100

  private val form = new UnauthorisedPaymentRecipientNameFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
