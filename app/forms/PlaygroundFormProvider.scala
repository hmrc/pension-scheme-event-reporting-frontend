package forms

import java.time.LocalDate

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class PlaygroundFormProvider @Inject() extends Mappings {

  def apply(): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey                    = "playground.error.invalid",
        threeDateComponentsMissingKey = "playground.error.required.all",
        twoDateComponentsMissingKey   = "playground.error.required.two",
        oneDateComponentMissingKey    = "playground.error.required"
      )
    )
}
