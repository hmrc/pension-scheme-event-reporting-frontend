package forms.event1

import forms.behaviours.IntFieldBehaviours
import play.api.data.FormError

class PaymentValueAndDateFormProviderSpec extends IntFieldBehaviours {

  private val form = new PaymentValueAndDateFormProvider()()

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
      nonNumericError  = FormError(fieldName, "paymentValueAndDate.error.nonNumeric"),
      wholeNumberError = FormError(fieldName, "paymentValueAndDate.error.wholeNumber")
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      minimum       = minimum,
      maximum       = maximum,
      expectedError = FormError(fieldName, "paymentValueAndDate.error.outOfRange", Seq(minimum, maximum))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, "paymentValueAndDate.error.required")
    )
  }
}
