package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class DeclarationPspFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "declarationPsp.error.required"
  private val lengthKey = "declarationPsp.error.length"
  private val maxLength = 100

  private val form = new DeclarationPspFormProvider()()

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
