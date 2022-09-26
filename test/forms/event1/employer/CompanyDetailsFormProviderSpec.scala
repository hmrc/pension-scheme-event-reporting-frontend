package forms.event1.employer

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class CompanyDetailsFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "companyDetails.error.required"
  val lengthKey = "companyDetails.error.length"
  val maxLength = 100

  val form = new CompanyDetailsFormProvider()()

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
