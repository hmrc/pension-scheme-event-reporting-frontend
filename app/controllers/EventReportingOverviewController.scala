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

package controllers

import config.FrontendAppConfig
import connectors.{EventReportingConnector, UserAnswersCacheConnector}
import controllers.EventReportingOverviewController.OverviewViewModel
import controllers.actions._
import controllers.partials.EventReportingTileController.{maxEndDateAsString, minStartDateAsString}
import models.enumeration.JourneyStartType
import models.enumeration.JourneyStartType.{InProgress, PastEventTypes}
import models.enumeration.VersionStatus.{Compiled, NotStarted, Submitted}
import models.{EROverview, TaxYear, UserAnswers, VersionInfo}
import pages.{EmptyWaypoints, EventReportingOverviewPage, EventReportingTileLinksPage, TaxYearPage, VersionInfoPage, Waypoints}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.EventReportingOverviewView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
class EventReportingOverviewController @Inject()(
                                         val controllerComponents: MessagesControllerComponents,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         connector: EventReportingConnector,
                                         userAnswersCacheConnector: UserAnswersCacheConnector,
                                         config: FrontendAppConfig,
                                         view: EventReportingOverviewView
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData() andThen requireData).async { implicit request =>

    val ua = request.userAnswers



    val ovm = for {
      pastYears <- getPastYearsAndUrl(ua, request.pstr)
      inProgressYears <- getInProgressYearAndUrl(ua, request.pstr)
      seqEROverview <- connector.getOverview(request.pstr, "ER", minStartDateAsString, maxEndDateAsString)
      isAnySubmittedReports = seqEROverview.exists(_.versionDetails.exists(_.submittedVersionAvailable))
      isAnyCompiledReports = seqEROverview.exists(_.versionDetails.exists(_.compiledVersionAvailable))
    } yield OverviewViewModel(pastYears = pastYears, yearsInProgress = inProgressYears, schemeName = request.schemeName,
      isAnyCompiledReports = isAnyCompiledReports, isAnySubmittedReports = isAnySubmittedReports)


      ovm.map( y => Ok(view(y)))
  }

  def onSubmit(taxYear: String, journeyType: String,  waypoints: Waypoints = EmptyWaypoints): Action[AnyContent] = (identify andThen getData() andThen requireData).async {
    implicit request =>

      val jt = journeyType match {
        case "InProgress" => InProgress
        case "PastEventTypes" => PastEventTypes
        case _ => InProgress
      }
      //println(s"TaxYearController: onSubmit ************ ${request.userAnswers.get(TaxYearPage)}")
      val originalUserAnswers = request.userAnswers
      val vd = originalUserAnswers
        .get(EventReportingOverviewPage).toSeq.flatten.find(_.taxYear.startYear == taxYear).flatMap(_.versionDetails)
      val versionInfo =
        (vd.map(_.compiledVersionAvailable), vd.map(_.submittedVersionAvailable), vd.map(_.numberOfVersions)) match {
          case (Some(true), _, Some(versions)) => VersionInfo(versions, Compiled)
          case (_, Some(true), Some(versions)) => VersionInfo(versions, Submitted)
          case _ => VersionInfo(1, NotStarted)
        }

      val futureAfterClearDown = request.userAnswers.get(TaxYearPage) match {
        case Some(v) if v.startYear != taxYear => userAnswersCacheConnector.removeAll(request.pstr)
        case _ => Future.successful((): Unit)
      }

      val updatedAnswers = originalUserAnswers
        .setOrException(TaxYearPage, TaxYear(taxYear), nonEventTypeData = true)
        .setOrException(EventReportingTileLinksPage, jt, nonEventTypeData = true)
        .setOrException(VersionInfoPage, versionInfo, nonEventTypeData = true)
      futureAfterClearDown.flatMap { _ =>
        userAnswersCacheConnector.save(request.pstr, updatedAnswers).map { _ =>
          Redirect(TaxYearPage.navigate(waypoints, originalUserAnswers, updatedAnswers).route)
        }
      }
  }

  private def getInProgressYearAndUrl(userAnswers: UserAnswers, pstr: String)(implicit hc: HeaderCarrier): Future[Seq[(String, String)]] = {

    userAnswersCacheConnector.get(pstr) flatMap { ua =>
      println(s"getPastYearsAndUrl 104 >>>>>>>>>>>>>>>> ${ua.get.get(EventReportingOverviewPage)}")

      val uaFetched = ua.fold(userAnswers)(x => x)
      uaFetched.get(EventReportingOverviewPage) match {
        case Some(s) =>
          println(s"getInProgressYearAndUrl 104 >>>>>>>>>>>>>>>> $s")
          val compiledVersionsOnly = s.filter(_.versionDetails.exists(_.compiledVersionAvailable))
          compiledVersionsOnly match {
            case Seq(erOverview) =>

              println(s"EventReportingTileLinksController onClickCompiled:51 >>>>>>>>>>>>>>>> $erOverview")
              val version = erOverview.versionDetails.map(_.numberOfVersions).getOrElse(1)
              val versionInfo = VersionInfo(version, Compiled)
              val ua = uaFetched
                .setOrException(TaxYearPage, erOverview.taxYear, nonEventTypeData = true)
                .setOrException(EventReportingTileLinksPage, InProgress, nonEventTypeData = true)
                .setOrException(VersionInfoPage, versionInfo, nonEventTypeData = true)

              userAnswersCacheConnector.save(pstr, ua).map { _ =>
                Seq((s"${erOverview.taxYear.startYear} to ${erOverview.taxYear.endYear}", routes.EventReportingOverviewController.onSubmit(erOverview.taxYear.startYear, "InProgress").url))
              }
            case _ =>
              println(s"getInProgressYearAndUrl :121 >>>>>>>>>>>>>>>> $compiledVersionsOnly")
              val uaUpdated = uaFetched.setOrException(EventReportingTileLinksPage, InProgress, nonEventTypeData = true)
              userAnswersCacheConnector.save(pstr, uaUpdated).map { x =>
                println("")
                getTaxYears(uaUpdated).map(x => (s"${x.startYear} to ${x.endYear}", routes.EventReportingOverviewController.onSubmit(x.startYear, "InProgress").url))
              }
          }
        case _ =>
          println(s"getInProgressYearAndUrl :128 >>>>>>>>>>>>>>>> ${uaFetched}")
          val uaUpdated = uaFetched.setOrException(EventReportingTileLinksPage, InProgress, nonEventTypeData = true)
          userAnswersCacheConnector.save(pstr, uaUpdated).map { _ =>
            getTaxYears(uaUpdated).map(x =>   (s"${x.startYear} to ${x.endYear}", routes.EventReportingOverviewController.onSubmit(x.startYear, "InProgress").url )  )
          }
      }
    }
  }


  private def getPastYearsAndUrl(userAnswers: UserAnswers, pstr: String)(implicit hc: HeaderCarrier): Future[Seq[(String, String)]] = {

    userAnswersCacheConnector.get(pstr) flatMap { ua =>
      println(s"getPastYearsAndUrl 104 >>>>>>>>>>>>>>>> ${ua.get.get(EventReportingOverviewPage)}")

      val uaFetched = ua.fold(userAnswers)(x => x)
      uaFetched.get(EventReportingOverviewPage) match {
        case Some(s: Seq[EROverview]) =>
          println(s"EventReportingTileLinksController onClickSubmitted:91 >>>>>>>>>>>>>>>> $s")
          val uaUpdated = uaFetched.setOrException(EventReportingTileLinksPage, PastEventTypes, nonEventTypeData = true)
          userAnswersCacheConnector.save(pstr, uaUpdated).map { _ =>
            getTaxYears(uaUpdated).map(x => (s"${x.startYear} to ${x.endYear}", routes.EventReportingOverviewController.onSubmit(x.startYear, "PastEventTypes").url) )
          }

        case _ =>
          println(s"EventReportingTileLinksController onClickSubmitted:99 >>>>>>>>>>>>>>>> ${uaFetched}")
          Future.successful(getTaxYears(uaFetched).map(x => (s"${x.startYear} to ${x.endYear}", routes.EventReportingOverviewController.onSubmit(x.startYear, "PastEventTypes").url)))
      }
    }
  }
  private def getTaxYears(ua: UserAnswers): Seq[TaxYear] = {
        (ua.get(EventReportingTileLinksPage), ua.get(EventReportingOverviewPage)) match {
          case (Some(PastEventTypes), Some(seqEROverview)) =>
            println(s">>>>> EventReportingOverviewController: getTaxYears ************ $seqEROverview")
            val applicableYears: Seq[String] = seqEROverview.flatMap(yearsWhereSubmittedVersionAvailable)
            TaxYear.optionsFilteredTaxYear(taxYear => applicableYears.contains(taxYear.startYear))
          case (Some(InProgress), Some(seqEROverview)) =>
            println(s">>>>> EventReportingOverviewController: getTaxYears ************ $seqEROverview")
            val applicableYears: Seq[String] = seqEROverview.flatMap(yearsWhereCompiledVersionAvailable)
            TaxYear.optionsFilteredTaxYear(taxYear => applicableYears.contains(taxYear.startYear))
          case _ =>
            println(s">>>>> EventReportingOverviewController: eventReportingStartTaxYear ************ ${config.eventReportingStartTaxYear}")
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

object EventReportingOverviewController {
  case class OverviewViewModel(
                                returnUrl: String = "returnUrl",
                                newAftUrl: String = "newAftUrl",
                                paymentsAndChargesUrl: String = "paymentsAndChargesUrl",
                                schemeName: String =  "schemeName",
                                outstandingAmount: String = "outstandingAmount",
                                yearsInProgress: Seq[(String, String)] = Seq.empty,
                                pastYears: Seq[(String, String)],
                                viewAllPastAftsUrl: String = "viewAllPastAftsUrl",
                                isAnySubmittedReports: Boolean = false,
                                isAnyCompiledReports: Boolean = false
                              )
}
