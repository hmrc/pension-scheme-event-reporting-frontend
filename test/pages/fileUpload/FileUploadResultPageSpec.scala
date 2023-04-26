package pages.fileUpload

import models.fileUpload.FileUploadResult
import pages.behaviours.PageBehaviours

class FileUploadResultSpec extends PageBehaviours {

  "FileUploadResultPage" - {

    beRetrievable[FileUploadResult](FileUploadResultPage)

    beSettable[FileUploadResult](FileUploadResultPage)

    beRemovable[FileUploadResult](FileUploadResultPage)
  }
}
