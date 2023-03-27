package forms.controllers.event13

import forms.behaviours.OptionFieldBehaviours
import models.controllers.event13.SchemeStructure
import play.api.data.FormError

class SchemeStructureFormProviderSpec extends OptionFieldBehaviours {

  private val form = new SchemeStructureFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "schemeStructure.error.required"

    behave like optionsField[SchemeStructure](
      form,
      fieldName,
      validValues  = SchemeStructure.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
