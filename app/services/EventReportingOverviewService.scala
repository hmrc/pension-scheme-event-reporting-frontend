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

package services

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.routes
import models.enumeration.JourneyStartType.{InProgress, PastEventTypes, StartNew}
import models.enumeration.VersionStatus.Compiled
import models.{EROverview, TaxYear, UserAnswers, VersionInfo}
import pages.{EventReportingOverviewPage, EventReportingTileLinksPage, TaxYearPage, VersionInfoPage}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class EventReportingOverviewService @Inject()(
                                               userAnswersCacheConnector: UserAnswersCacheConnector,
                                               config: FrontendAppConfig
                                             ) (implicit ec: ExecutionContext) {

  def getStartNewUrl(userAnswers: UserAnswers, pstr: String)(implicit hc: HeaderCarrier): Future[String] = {
    val ua = userAnswers.setOrException(EventReportingTileLinksPage, StartNew, nonEventTypeData = true)
    userAnswersCacheConnector.save(pstr, ua).map { _ =>
      config.erStartNewUrl
    }
  }
  def getInProgressYearAndUrl(userAnswers: UserAnswers, pstr: String)(implicit hc: HeaderCarrier): Future[Seq[(String, String)]] = {

    userAnswersCacheConnector.get(pstr) flatMap { ua =>
      val uaFetched = ua.fold(userAnswers)(x => x)
      uaFetched.get(EventReportingOverviewPage) match {
        case Some(s) =>
          val compiledVersionsOnly = s.filter(_.versionDetails.exists(_.compiledVersionAvailable))
          compiledVersionsOnly match {
            case Seq(erOverview) =>

              val version = erOverview.versionDetails.map(_.numberOfVersions).getOrElse(1)
              val versionInfo = VersionInfo(version, Compiled)
              val ua = uaFetched
                .setOrException(TaxYearPage, erOverview.taxYear, nonEventTypeData = true)
                .setOrException(EventReportingTileLinksPage, InProgress, nonEventTypeData = true)
                .setOrException(VersionInfoPage, versionInfo, nonEventTypeData = true)

              userAnswersCacheConnector.save(pstr, ua).map { _ =>
                Seq((s"6 April ${erOverview.taxYear.startYear} to 5 April ${erOverview.taxYear.endYear}", routes.EventReportingOverviewController.onSubmit(erOverview.taxYear.startYear, "InProgress").url))
              }
            case _ =>
              val uaUpdated = uaFetched.setOrException(EventReportingTileLinksPage, InProgress, nonEventTypeData = true)
              userAnswersCacheConnector.save(pstr, uaUpdated).map { x =>
                getTaxYears(uaUpdated).map(x => (s"6 April ${x.startYear} to 5 April ${x.endYear}", routes.EventReportingOverviewController.onSubmit(x.startYear, "InProgress").url))
              }
          }
        case _ =>
          val uaUpdated = uaFetched.setOrException(EventReportingTileLinksPage, InProgress, nonEventTypeData = true)
          userAnswersCacheConnector.save(pstr, uaUpdated).map { _ =>
            getTaxYears(uaUpdated).map(x =>   (s"6 April ${x.startYear} to 5 April ${x.endYear}", routes.EventReportingOverviewController.onSubmit(x.startYear, "InProgress").url )  )
          }
      }
    }
  }

  def linkForOutstandingAmount(srn: String, outstandingAmount: String): String = {
    if (outstandingAmount == "£0.00") {
      config.financialOverviewURL.format(srn)
    } else {
      config.selectChargesYearURL.format(srn, "event-reporting")
    }
  }

  def getPastYearsAndUrl(userAnswers: UserAnswers, pstr: String)(implicit hc: HeaderCarrier): Future[Seq[(String, String)]] = {

    userAnswersCacheConnector.get(pstr) flatMap { ua =>

      val uaFetched = ua.fold(userAnswers)(x => x)
      uaFetched.get(EventReportingOverviewPage) match {
        case Some(s: Seq[EROverview]) =>
          val uaUpdated = uaFetched.setOrException(EventReportingTileLinksPage, PastEventTypes, nonEventTypeData = true)
          userAnswersCacheConnector.save(pstr, uaUpdated).map { _ =>
            getTaxYears(uaUpdated).map(x => (s"6 April ${x.startYear} to 5 April ${x.endYear}", routes.EventReportingOverviewController.onSubmit(x.startYear, "PastEventTypes").url) )
          }

        case _ =>
          Future.successful(getTaxYears(uaFetched).map(x => (s"6 April ${x.startYear} to 5 April ${x.endYear}", routes.EventReportingOverviewController.onSubmit(x.startYear, "PastEventTypes").url)))
      }
    }
  }

  def getTaxYears(ua: UserAnswers): Seq[TaxYear] = {
    (ua.get(EventReportingTileLinksPage), ua.get(EventReportingOverviewPage)) match {
      case (Some(PastEventTypes), Some(seqEROverview)) =>
        val applicableYears: Seq[String] = seqEROverview.flatMap(yearsWhereSubmittedVersionAvailable)
        TaxYear.optionsFilteredTaxYear(taxYear => applicableYears.contains(taxYear.startYear))
      case (Some(InProgress), Some(seqEROverview)) =>
        val applicableYears: Seq[String] = seqEROverview.flatMap(yearsWhereCompiledVersionAvailable)
        TaxYear.optionsFilteredTaxYear(taxYear => applicableYears.contains(taxYear.startYear))
      case _ =>
        TaxYear.optionsFilteredTaxYear( taxYear =>  taxYear.startYear.toInt >= config.eventReportingStartTaxYear)
    }
  }

  private val yearsWhereSubmittedVersionAvailable: EROverview => Seq[String] = erOverview =>
    if (erOverview.versionDetails.exists(_.submittedVersionAvailable)) {
      Seq(erOverview.taxYear.startYear)
    } else {
      Nil
    }

  private val yearsWhereCompiledVersionAvailable: EROverview => Seq[String] = erOverview =>
    if (erOverview.versionDetails.exists(_.compiledVersionAvailable)) {
      Seq(erOverview.periodStartDate.getYear.toString)
    } else {
      Nil
    }

}
