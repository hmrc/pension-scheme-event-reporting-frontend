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

package config

import com.google.inject.{Inject, Singleton}
import models.enumeration.AdministratorOrPractitioner
import models.enumeration.AdministratorOrPractitioner.Administrator
import models.UploadId
import models.enumeration.EventType
import models.enumeration.EventType.toRoute
import play.api.Configuration
import play.api.i18n.Lang
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject()(configuration: Configuration, servicesConfig: ServicesConfig) {

  private def loadConfig(key: String): String =
    configuration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private def getConfigString(key: String) = servicesConfig.getConfString(key,
    throw new Exception(s"Could not find config '$key'"))

  val host: String = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  lazy val eventReportingUrl: String = servicesConfig.baseUrl("pension-scheme-event-reporting")
  lazy val pensionsAdministratorUrl: String = servicesConfig.baseUrl("pension-administrator")
  lazy val addressLookUp: String = s"${servicesConfig.baseUrl("address-lookup")}"

  lazy val locationCanonicalList: String = loadConfig("location.canonical.list")

  val betaFeedbackUnauthenticatedUrl: String = getConfigString("contact-frontend.beta-feedback-url.unauthenticated")

  lazy val erLoginUrl: String = configuration.get[String](path = "urls.partials.erLoginLink")
  val loginUrl: String = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String = configuration.get[String]("urls.signOut")

  def administratorOrPractitionerUrl: String = loadConfig("urls.administratorOrPractitioner")

  def youNeedToRegisterUrl: String = loadConfig("urls.youNeedToRegisterPage")

  def yourPensionSchemesUrl: String = loadConfig("urls.yourPensionSchemes")

  def successEndPointTarget(eventType: EventType): String = loadConfig("upscan.success-endpoint").format(toRoute(eventType))
  def failureEndPointTarget(eventType: EventType): String = loadConfig("upscan.failure-endpoint").format(toRoute(eventType))

  lazy val maxUploadFileSize: Int = configuration.getOptional[Int]("upscan.maxUploadFileSizeMb").getOrElse(1)

  lazy val initiateV2Url:String = servicesConfig.baseUrl("upscan-initiate") + "/upscan/v2/initiate"

  private val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  val exitSurveyUrl: String = s"$exitSurveyBaseUrl/feedback/PODS"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")
  lazy val minimumYear: Int = configuration.get[Int]("minimumYear")

  lazy val minimalDetailsUrl: String = s"$pensionsAdministratorUrl${configuration.get[String](path = "urls.minimalDetails")}"
  lazy val parsingAndValidationUrl: String = s"$eventReportingUrl${configuration.get[String](path = "urls.parsingAndValidation")}"

  lazy val emailApiUrl: String = servicesConfig.baseUrl("email")
  lazy val emailSendForce: Boolean = configuration.getOptional[Boolean]("email.force").getOrElse(false)
  lazy val fileReturnTemplateId: String = configuration.get[String]("email.fileReturnTemplateId")

  def eventReportingEmailCallback(
                                   schemeAdministratorType: AdministratorOrPractitioner,
                                   requestId: String,
                                   encryptedEmail: String,
                                   encryptedPsaId: String
                                 ) = s"$eventReportingUrl${
    configuration.get[String](path = "urls.emailCallback")
      .format(
        if (schemeAdministratorType == Administrator) "PSA" else "PSP",
        requestId,
        encryptedEmail,
        encryptedPsaId
      )
  }"
}
