package forms

import forms.behaviours.CheckboxFieldBehaviours
import models.TestCheckBox
import play.api.data.FormError

class TestCheckBoxFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new TestCheckBoxFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "testCheckBox.error.required"

    behave like checkboxField[TestCheckBox](
      form,
      fieldName,
      validValues  = TestCheckBox.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
