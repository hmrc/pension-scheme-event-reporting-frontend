package forms.address

import forms.behaviours.OptionFieldBehaviours
import models.address.ChooseAddress
import play.api.data.FormError

class ChooseAddressFormProviderSpec extends OptionFieldBehaviours {

  val form = new ChooseAddressFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "chooseAddress.error.required"

    behave like optionsField[ChooseAddress](
      form,
      fieldName,
      validValues  = ChooseAddress.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
