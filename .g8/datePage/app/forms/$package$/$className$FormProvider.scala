package forms$if(package.empty)$$else$.$package$$endif$

import java.time.LocalDate

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey                    = "$className;format="decap"$.error.invalid",
        threeDateComponentsMissingKey = "$className;format="decap"$.error.required.all",
        twoDateComponentsMissingKey   = "$className;format="decap"$.error.required.two",
        oneDateComponentMissingKey    = "$className;format="decap"$.error.required"
      )
    )
}
