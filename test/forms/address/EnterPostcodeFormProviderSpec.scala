package forms.address

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class EnterPostcodeFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "enterPostcode.error.required"
  val lengthKey = "enterPostcode.error.length"
  val maxLength = 100

  val form = new EnterPostcodeFormProvider()()

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
