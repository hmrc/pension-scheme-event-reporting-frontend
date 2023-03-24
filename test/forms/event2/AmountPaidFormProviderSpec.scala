package forms.event2

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class AmountPaidFormProviderSpec extends IntFieldBehaviours {

  private val form = new AmountPaidFormProvider()()

  ".value" - {

    val fieldName = "value"

    val minimum = 0
    val maximum = Int.MaxValue

    val validDataGenerator = intsInRangeWithCommas(minimum, maximum)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validDataGenerator
    )

    behave like intField(
      form,
      fieldName,
      nonNumericError  = FormError(fieldName, "amountPaid.event2.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "amountPaid.event2.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "amountPaid.event2.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "amountPaid.event2.error.required")
    )
  }
}
