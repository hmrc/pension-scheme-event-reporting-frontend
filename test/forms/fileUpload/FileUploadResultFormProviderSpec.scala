package forms.fileUpload

import forms.behaviours.OptionFieldBehaviours
import models.fileUpload.FileUploadResult
import play.api.data.FormError

class FileUploadResultFormProviderSpec extends OptionFieldBehaviours {

  private val form = new FileUploadResultFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "fileUploadResult.error.required"

    behave like optionsField[FileUploadResult](
      form,
      fieldName,
      validValues  = FileUploadResult.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
