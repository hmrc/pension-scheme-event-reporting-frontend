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
import models.enumeration.EventType
import models.enumeration.EventType.{Event6, Event22, Event23}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment
import play.api.test.Helpers.baseApplicationBuilder.injector

import java.io.File

class FileProviderServiceSpec extends SpecBase with MockitoSugar {

  private val environment: Environment = injector.instanceOf[Environment]
  private val fileProviderService = new FileProviderService(environment)
  //noinspection ScalaStyle
  private val instructionsFileToCheckEvent6 = new File("./conf/fileDownload/instructions/instructions-event-6-benefit-crystallisation-where-the-member-relies-on-LTA-protections.ods")
  private val instructionsFileToCheckEvent22 = new File("./conf/fileDownload/instructions/instructions-event-22-annual-allowance.ods")
  private val instructionsFileToCheckEvent23 = new File("./conf/fileDownload/instructions/instructions-event-23-dual-annual-allowance.ods")

  "FileProviderService Service" - {

    testReturnCorrectInstructionsFile(Event6, instructionsFileToCheckEvent6)
    testReturnCorrectInstructionsFile(Event22, instructionsFileToCheckEvent22)
    testReturnCorrectInstructionsFile(Event23, instructionsFileToCheckEvent23)
    testReturnCorrectTemplateFile(Event6)
    testReturnCorrectTemplateFile(Event22)
    testReturnCorrectTemplateFile(Event23)
  }


  private def testReturnCorrectInstructionsFile(eventType: EventType, instructionsFile: File): Unit = {
    s"must return the correct instructions file for Event $eventType" in {
      fileProviderService.getInstructionsFile(eventType) mustBe instructionsFile
    }
  }

  private def testReturnCorrectTemplateFile(eventType: EventType): Unit = {
    s"must return the correct template file for Event $eventType" in {
      val templateFileToCheck = new File(s"./conf/fileDownload/template/event-${eventType.toString}-bulk-upload.csv")
      fileProviderService.getTemplateFile(eventType) mustBe templateFileToCheck
    }
  }

}