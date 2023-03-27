package forms.event13

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class SchemeStructureDescriptionFormProviderSpec extends StringFieldBehaviours {

  private val lengthKey = "schemeStructureDescription.error.length"
  private val maxLength = 150

  private val form = new SchemeStructureDescriptionFormProvider()()

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
