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

import models.enumeration.EventType
import models.enumeration.EventType.{Event1, Event22, Event23, Event6}
import play.api.Environment

import java.io.File
import javax.inject.Inject

class FileProviderService @Inject()(environment: Environment) {
  private val baseInstructionsPath: String = "conf/fileDownload/instructions"
  private val baseTemplatePath: String = "conf/fileDownload/template"

  private val instructionsFilePathEvent1 = s"$baseInstructionsPath/instructions-event-1-unauthorised-payments.ods"
  private val templateFilePathEvent1 = s"$baseTemplatePath/event-1-bulk-upload.csv"

  private val instructionsFilePathEvent6 = s"$baseInstructionsPath/instructions-event-6-benefit-crystallisation-where-the-member-relies-on-LTA-protections.ods"
  private val templateFilePathEvent6 = s"$baseTemplatePath/event-6-bulk-upload.csv"

  private val instructionsFilePathEvent22 = s"$baseInstructionsPath/instructions-event-22-annual-allowance.ods"
  private val templateFilePathEvent22 = s"$baseTemplatePath/event-22-bulk-upload.csv"

  private val instructionsFilePathEvent23 = s"$baseInstructionsPath/instructions-event-23-dual-annual-allowance.ods"
  private val templateFilePathEvent23 = s"$baseTemplatePath/event-23-bulk-upload.csv"

  def getInstructionsFile(eventType: EventType): File = {
    val path: String = eventType match {
      case Event1 => instructionsFilePathEvent1
      case Event6 => instructionsFilePathEvent6
      case Event22 => instructionsFilePathEvent22
      case Event23 => instructionsFilePathEvent23
      case _ => throw new RuntimeException("No instructions file found for this event type")
    }
    environment.getFile(path)
  }

  def getTemplateFile(eventType: EventType): File = {
    val path: String = eventType match {
      case Event1 => templateFilePathEvent1
      case Event6 => templateFilePathEvent6
      case Event22 => templateFilePathEvent22
      case Event23 => templateFilePathEvent23
      case _ => throw new RuntimeException("No template file found for this event type")
    }
    environment.getFile(path)
  }
}