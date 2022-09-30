package forms$if(package.empty)$$else$.$package$$endif$

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[Option[String]] =
    Form(
      "value" -> optionalText()
        .verifying(maxLength($maxLength$, "$className$.error.length"))
    )
}
