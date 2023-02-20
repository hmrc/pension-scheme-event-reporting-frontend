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

import play.api.Environment

import java.io.File
import javax.inject.Inject

class FileProviderService @Inject()(environment: Environment) {
  private val baseInstructionsPath: String = "conf/fileDownload/instructions"
  private val baseTemplatePath: String = "conf/fileDownload/template"

  private val instructionsFilePath = s"$baseInstructionsPath/instructions-event-22-annual-allowance.ods"
  private val templateFilePath = s"$baseTemplatePath/event-22-bulk-upload.csv"

  def getInstructionsFile: File = {
    val path: String = instructionsFilePath
    environment.getFile(path)
  }

  def getTemplateFile: File = {
    val path: String = templateFilePath
    environment.getFile(path)
  }
}