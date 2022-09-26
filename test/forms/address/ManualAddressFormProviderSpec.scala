package forms.address

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ManualAddressFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "manualAddress.error.required"
  val lengthKey = "manualAddress.error.length"
  val maxLength = 100

  val form = new ManualAddressFormProvider()()

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
