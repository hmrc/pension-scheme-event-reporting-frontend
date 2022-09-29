package forms$if(package.empty)$$else$.$package$$endif$

import java.time.{LocalDate, ZoneOffset}

import forms.behaviours.DateBehaviours

class $className$FormProviderSpec extends DateBehaviours {

  private val form = new $className$FormProvider()()

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "$className;format="decap"$.error.required.all")
  }
}
