package forms.event13

import java.time.{LocalDate, ZoneOffset}

import forms.behaviours.DateBehaviours

class ChangeDateFormProviderSpec extends DateBehaviours {

  private val form = new ChangeDateFormProvider()()

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "changeDate.error.required.all")
  }
}
