package forms.event18

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class removeEvent23FormProvider @Inject() extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "value" -> boolean("removeEvent23.error.required")
    )
}
