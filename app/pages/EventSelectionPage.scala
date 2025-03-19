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

package pages

import controllers.routes
import models.enumeration.EventType
import models.enumeration.EventType._
import models.{EventSelection, Index, UserAnswers}
import pages.EventSelectionPageUtility.adjustedCount
import pages.common._
import pages.event1.PaymentValueAndDatePage
import pages.event11.{WhatYouWillNeedPage => event11WhatYouWillNeed}
import pages.event12.HasSchemeChangedRulesPage
import pages.event18.Event18ConfirmationPage
import pages.event19.{WhatYouWillNeedPage => event19WhatYouWillNeed}
import pages.event2.DatePaidPage
import pages.event24.OverAllowanceAndDeathBenefitPage
import pages.event6.AmountCrystallisedAndDatePage
import pages.event7.PaymentDatePage
import pages.event8.LumpSumAmountAndDatePage
import pages.eventWindUp.SchemeWindUpDatePage
import play.api.Logger
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object EventSelectionPage extends QuestionPage[EventSelection] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "EventSelection"

  override def route(waypoints: Waypoints): Call =
    routes.EventSelectionController.onPageLoad(waypoints)

  //scalastyle:off cyclomatic.complexity
  override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    val optionEventType = answers.get(this).flatMap(es => EventType.fromEventSelection(es))
    val index = adjustedCount(optionEventType, answers)
    optionEventType match {
      case Some(bulkUploadEvent@(Event1 | Event6 | Event22 | Event23)) =>
        ManualOrUploadPage(bulkUploadEvent, index)
      case Some(memberBasedEvent@(Event3 | Event4 | Event5 | Event7 | Event8 | Event8A)) =>
        WhatYouWillNeedPage(memberBasedEvent, index)
      case Some(Event2) => event2.WhatYouWillNeedPage(index)
      case Some(Event24) =>
        event24.WhatYouWillNeedPage(index)
      case Some(Event10) => event10.WhatYouWillNeedPage
      case Some(Event11) => event11WhatYouWillNeed
      case Some(Event12) => HasSchemeChangedRulesPage
      case Some(Event13) => event13.WhatYouWillNeedPage
      case Some(Event14) => event14.WhatYouWillNeedPage
      case Some(Event18) => Event18ConfirmationPage
      case Some(Event19) => event19WhatYouWillNeed
      case Some(Event20) => event20.WhatYouWillNeedPage
      case Some(Event20A) => event20A.WhatYouWillNeedPage
      case Some(WindUp) => SchemeWindUpDatePage
      case _ => JourneyRecoveryPage
    }
  }
}

private object EventSelectionPageUtility {

  private val logger = Logger(classOf[EventSelection])

  def adjustedCount(maybeEventType: Option[EventType], userAnswers: UserAnswers): Int = {

    val countForEvent = maybeEventType match {
      case Some(Event1) => userAnswers.countAll(MembersOrEmployersPage(Event1))
      case Some(memberBasedEvent@(
        Event2 | Event3 | Event4 | Event5 | Event6 |
        Event7 | Event8 | Event8A | Event22 | Event23 | Event24
        )) => userAnswers.countAll(MembersPage(memberBasedEvent))
      case _ => 0
    }

    val rangeAsList = (0 until countForEvent).toList

    val getIndex = Index.intToIndex _

    val questionWasAnswered: Option[Any] => Boolean = {
      case Some(_) => true
      case None => false
    }

    val finalPageInMemberBasedJourney: Int => Boolean = (int: Int) => maybeEventType match {
      case Some(Event1) =>
        questionWasAnswered(userAnswers.get(PaymentValueAndDatePage(getIndex(int))))
      case Some(Event2) =>
        questionWasAnswered(userAnswers.get(DatePaidPage(getIndex(int), Event2)))
      case Some(event3or4or5@(Event3 | Event4 | Event5)) =>
        questionWasAnswered(userAnswers.get(PaymentDetailsPage(event3or4or5, getIndex(int))))
      case Some(Event6) =>
        questionWasAnswered(userAnswers.get(AmountCrystallisedAndDatePage(Event6, getIndex(int))))
      case Some(Event7) =>
        questionWasAnswered(userAnswers.get(PaymentDatePage(getIndex(int))))
      case Some(event8or8a@(Event8 | Event8A)) =>
        questionWasAnswered(userAnswers.get(LumpSumAmountAndDatePage(event8or8a, getIndex(int))))
      case Some(event22or23@(Event22 | Event23)) =>
        questionWasAnswered(userAnswers.get(TotalPensionAmountsPage(event22or23, getIndex(int))))
      case Some(Event24) =>
        questionWasAnswered(userAnswers.get(OverAllowanceAndDeathBenefitPage(getIndex(int))))
      case _ => false
    }

    val memberCompiled: Int => Boolean = (int: Int) => maybeEventType match {
      case Some(eventType) =>
        val hasMemberStatus = {
          val path = eventType match {
            case EventType.Event1 => MembersOrEmployersPage(eventType)(int) \ "memberStatus"
            case _ => MembersPage(eventType)(int) \ "memberStatus"
          }
          userAnswers.get(path).map(_ => true).getOrElse(false)
        }
        val hasBeenCompiledInSession = userAnswers.get(MemberCompiled(eventType, int)).getOrElse(false)
        hasMemberStatus || hasBeenCompiledInSession
      case None => false
    }


    val indicesForIncompleteJourneys = rangeAsList.collect {
      case i if !finalPageInMemberBasedJourney(i) => getIndex(i)
      case i if !memberCompiled(i) => getIndex(i)
    }

    if (indicesForIncompleteJourneys.nonEmpty) {
      logger.info(
        s"""Journey for Event$maybeEventType incomplete on indices: $indicesForIncompleteJourneys.
           | Directing user to complete journey at index: ${indicesForIncompleteJourneys.head}""".stripMargin
      )
      indicesForIncompleteJourneys.head
    } else {
      countForEvent
    }
  }
}
