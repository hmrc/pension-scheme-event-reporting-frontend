package forms$if(package.empty)$$else$.$package$$endif$

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class $className$FormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "$className;format="decap"$.error.required"
  private val lengthKey = "$className;format="decap"$.error.length"
  private val maxLength = $maxLength$

  private val form = new $className$FormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )
  }
}
