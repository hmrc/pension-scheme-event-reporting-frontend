package forms.event11

import java.time.{LocalDate, ZoneOffset}

import forms.behaviours.DateBehaviours

class InvestmentsInAssetsRuleChangeDateFormProviderSpec extends DateBehaviours {

  private val form = new InvestmentsInAssetsRuleChangeDateFormProvider()()

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "investmentsInAssetsRuleChangeDate.error.required.all")
  }
}
