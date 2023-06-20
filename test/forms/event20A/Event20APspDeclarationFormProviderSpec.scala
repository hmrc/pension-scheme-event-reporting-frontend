package forms.event20A

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class Event20APspDeclarationFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "event20APspDeclaration.error.required"
  private val lengthKey = "event20APspDeclaration.error.length"
  private val maxLength = 100

  private val form = new Event20APspDeclarationFormProvider()()

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
