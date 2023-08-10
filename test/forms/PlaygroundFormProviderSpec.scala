package forms

import java.time.{LocalDate, ZoneOffset}

import forms.behaviours.DateBehaviours

class PlaygroundFormProviderSpec extends DateBehaviours {

  private val form = new PlaygroundFormProvider()()

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "playground.error.required.all")
  }
}
