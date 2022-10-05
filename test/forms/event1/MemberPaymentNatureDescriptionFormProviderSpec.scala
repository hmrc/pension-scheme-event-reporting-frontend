package forms.event1

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class MemberPaymentNatureDescriptionFormProviderSpec extends StringFieldBehaviours {

  private val lengthKey = "memberPaymentNatureDescription.error.length"
  private val maxLength = 150

  private val form = new MemberPaymentNatureDescriptionFormProvider()()

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
  }
}
