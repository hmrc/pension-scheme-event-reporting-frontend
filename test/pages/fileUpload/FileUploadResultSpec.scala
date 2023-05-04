/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pages.fileUpload

import models.enumeration.EventType.Event22
import models.fileUpload.FileUploadResult
import pages.behaviours.PageBehaviours

class FileUploadResultSpec extends PageBehaviours {

  "FileUploadResultPage" - {

    beRetrievable[FileUploadResult](FileUploadResultPage(eventType = Event22))

    beSettable[FileUploadResult](FileUploadResultPage(eventType = Event22))

    beRemovable[FileUploadResult](FileUploadResultPage(eventType = Event22))
  }
}
