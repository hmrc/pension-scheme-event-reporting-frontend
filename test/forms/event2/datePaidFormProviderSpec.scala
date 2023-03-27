package forms.event2

import java.time.{LocalDate, ZoneOffset}

import forms.behaviours.DateBehaviours

class datePaidFormProviderSpec extends DateBehaviours {

  private val form = new datePaidFormProvider()()

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "datePaid.event2.error.required.all")
  }
}
