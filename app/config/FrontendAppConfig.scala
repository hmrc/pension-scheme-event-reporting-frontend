/*
 * Copyright 2024 HM Revenue & Customs
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
import models.enumeration.AdministratorOrPractitioner.Administrator
import models.enumeration.EventType.toRoute
import models.enumeration.{AdministratorOrPractitioner, EventType}
import play.api.Configuration
import play.api.i18n.Lang
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.Duration

@Singleton
class FrontendAppConfig @Inject()(configuration: Configuration, servicesConfig: ServicesConfig) {

  private def loadConfig(key: String): String =
    configuration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private def getConfigString(key: String) = servicesConfig.getConfString(key,
    throw new Exception(s"Could not find config '$key'"))

  val host: String = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  val ifsTimeout: Duration = configuration.get[Duration]("ifs.timeout")

  lazy val eventReportingUrl: String = servicesConfig.baseUrl("pension-scheme-event-reporting")
  lazy val pensionsAdministratorUrl: String = servicesConfig.baseUrl("pension-administrator")
  lazy val pensionSchemeUrl: String = servicesConfig.baseUrl("pensions-scheme")
  lazy val addressLookUp: String = s"${servicesConfig.baseUrl("address-lookup")}"
  lazy val erOutstandingPaymentAmountURL: String = s"${servicesConfig.baseUrl("aft-frontend")}" + configuration.get[String]("urls.erOutstandingPaymentAmountURL")
  lazy val financialOverviewURL: String = configuration.get[String](path = "urls.financialOverviewURL")
  lazy val selectChargesYearURL: String = configuration.get[String](path = "urls.selectChargesYearURL")
  lazy val locationCanonicalList: String = loadConfig("location.canonical.list.all")
  lazy val locationCanonicalListEUAndEEA: String = loadConfig("location.canonical.list.UKEUAndEEA")

  val betaFeedbackUnauthenticatedUrl: String = getConfigString("contact-frontend.beta-feedback-url.unauthenticated")

  lazy val erStartNewUrl: String = configuration.get[String](path = "urls.partials.erStartNew")
  lazy val erCompiledUrl: String = configuration.get[String](path = "urls.partials.erCompiledLink")
  lazy val erSubmittedUrl: String = configuration.get[String](path = "urls.partials.erSubmittedLink")

  val loginUrl: String = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String = loadConfig("urls.signOut")
  val exitSurveyUrl: String = loadConfig("urls.feedback")
  val signOutNoSurveyUrl : String = s"$loginUrl?continue=$loginContinueUrl"

  def administratorOrPractitionerUrl: String = loadConfig("urls.administratorOrPractitioner")

  def youNeedToRegisterUrl: String = loadConfig("urls.youNeedToRegisterPage")

  def yourPensionSchemesUrl: String = loadConfig("urls.yourPensionSchemes")

  def schemeDashboardUrl(administratorOrPractitioner: AdministratorOrPractitioner, srn: String): String =
    (
      administratorOrPractitioner match {
        case AdministratorOrPractitioner.Administrator => schemeSummaryPsaUrl
        case AdministratorOrPractitioner.Practitioner => schemeSummaryPspUrl
      }
      ).format(srn)

  private lazy val schemeSummaryPsaUrl: String = loadConfig("urls.schemeSummaryPsa")
  private lazy val schemeSummaryPspUrl: String = loadConfig("urls.schemeSummaryPsp")

  def listPspUrl: String = loadConfig("urls.listPsp")

   def manageOverviewDashboardUrl: String = loadConfig("urls.manageOverviewDashboard")

  def contactHmrcURL: String = loadConfig("urls.contactHmrcURL")

  def successEndPointTarget(eventType: EventType): String = loadConfig("upscan.success-endpoint").format(toRoute(eventType))

  def validateEndPointTarget(eventType: EventType): String = loadConfig("upscan.validate-endpoint").format(toRoute(eventType))

  lazy val maxUploadFileSize: Int = configuration.getOptional[Int]("upscan.maxUploadFileSizeMb").getOrElse(1)

  lazy val initiateV2Url: String = servicesConfig.baseUrl("upscan-initiate") + "/upscan/v2/initiate"

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")
  lazy val minimumYear: Int = configuration.get[Int]("minimumYear")

  lazy val compileDelayInSeconds: Int = configuration.get[Int]("compileDelayInSeconds")

  lazy val minimalDetailsUrl: String = s"$pensionsAdministratorUrl${configuration.get[String](path = "urls.minimalDetails")}"
  lazy val parsingAndValidationUrl: String = s"$eventReportingUrl${configuration.get[String](path = "urls.parsingAndValidation")}"
  lazy val schemeDetailsUrl: String = s"$pensionSchemeUrl${configuration.get[String](path = "urls.schemeDetails")}"
  lazy val pspSchemeDetailsUrl: String = s"$pensionSchemeUrl${configuration.get[String](path = "urls.pspSchemeDetails")}"
  def openDateUrl(srn: String): String =
    s"$pensionSchemeUrl${configuration.get[String](path = "urls.openDate")}".format(srn)

  lazy val emailApiUrl: String = servicesConfig.baseUrl("email")
  lazy val emailSendForce: Boolean = configuration.getOptional[Boolean]("email.force").getOrElse(false)
  lazy val fileReturnTemplateId: String = configuration.get[String]("email.fileReturnTemplateId")
  lazy val eventReportingStartTaxYear: Int = configuration.get[Int]("eventReportingStartTaxYear")
  lazy val ltaAbolitionStartYear: Int = configuration.get[Int]("ltaAbolitionStartYear")

  def eventReportingEmailCallback(
                                   schemeAdministratorType: AdministratorOrPractitioner,
                                   requestId: String,
                                   encryptedEmail: String,
                                   encryptedPsaId: String,
                                   encryptedPstr: String,
                                   reportVersion: String
                                 ) = s"$eventReportingUrl${
    configuration.get[String](path = "urls.emailCallback")
      .format(
        if (schemeAdministratorType == Administrator) "PSA" else "PSP",
        requestId,
        encryptedEmail,
        encryptedPsaId,
        encryptedPstr,
        reportVersion
      )
  }"

  val validEvent1Header: String = configuration.get[String]("validEvent1Header")
  val validEvent6Header: String = configuration.get[String]("validEvent6Header")
  val validEvent22Header: String = configuration.get[String]("validEvent22Header")
  val validEvent23Header: String = configuration.get[String]("validEvent23Header")
  val validEvent24Header: String = configuration.get[String]("validEvent24Header")
}
