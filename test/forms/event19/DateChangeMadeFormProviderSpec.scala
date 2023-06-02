package forms.event19

import java.time.{LocalDate, ZoneOffset}

import forms.behaviours.DateBehaviours

class DateChangeMadeFormProviderSpec extends DateBehaviours {

  private val form = new DateChangeMadeFormProvider()()

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "dateChangeMade.error.required.all")
  }
}
