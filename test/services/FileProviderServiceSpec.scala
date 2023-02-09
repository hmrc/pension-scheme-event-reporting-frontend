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

package services

import base.SpecBase
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.test.Helpers.baseApplicationBuilder.injector

import java.io.File

class FileProviderServiceSpec extends SpecBase with MockitoSugar {

  private val environment: Environment = injector.instanceOf[Environment]
  private val fileProviderService = new FileProviderService(environment)

  "getInstructionsFile" - {

    "for event 22 what you will need page return the correct file name" in {

      val instructionsFileToCheck = new File("./conf/fileDownload/instructions/instructions-event-22-annual-allowance.ods")

      fileProviderService.getInstructionsFile mustBe instructionsFileToCheck

    }
  }

  "getTemplateFile" - {
    "for event 22 what you will need page return the correct file name" in {

      val templateFileToCheck =
        new File("./conf/fileDownload/template/event-22-bulk-upload.csv")

      fileProviderService.getTemplateFile mustBe templateFileToCheck

    }
  }

}