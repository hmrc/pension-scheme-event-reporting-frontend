/*
 * Copyright 2022 HM Revenue & Customs
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

package journey

import cats.data.State
import cats.implicits._
import models.UserAnswers
import org.scalactic.source.Position
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.{EmptyWaypoints, Page, PageAndWaypoints, WaypointPage, Waypoints}
import play.api.libs.json.{Reads, Writes}
import queries.{Gettable, Settable}

trait JourneyHelpers extends Matchers with TryValues with OptionValues {

  type JourneyStep[A] = State[JourneyState, A]

  final case class JourneyState(page: Page, waypoints: Waypoints, answers: UserAnswers) {

    def next: JourneyState = {
      val PageAndWaypoints(nextPage, newWaypoints) = page.navigate(waypoints, answers, answers)
      JourneyState(nextPage, newWaypoints, answers)
    }

    def next(originalAnswers: UserAnswers): JourneyState = {
      val PageAndWaypoints(nextPage, newWaypoints) = page.navigate(waypoints, originalAnswers, answers)
      JourneyState(nextPage, newWaypoints, answers)
    }

    def run(steps: JourneyStep[Unit]*): JourneyState =
      journeyOf(steps: _*).runS(this).value

    def check(steps: JourneyStep[Unit]*): JourneyState =
      run(steps: _*)
  }

  def journeyOf(steps: JourneyStep[Unit]*): JourneyStep[Unit] =
    steps.fold(State.pure(())) {
      _ >> _
    }

  def startingFrom(
                    page: Page,
                    waypoints: Waypoints = EmptyWaypoints,
                    answers: UserAnswers = UserAnswers()
                  ): JourneyState =
    JourneyState(page, waypoints, answers)

  def next: JourneyStep[Unit] =
    State.modify(_.next)

  def next(originalAnswers: UserAnswers): JourneyStep[Unit] =
    State.modify(_.next(originalAnswers))

  def getPage: JourneyStep[Page] =
    State.inspect(_.page)

  def getWaypoints: JourneyStep[Waypoints] =
    State.inspect(_.waypoints)

  def getAnswers: JourneyStep[UserAnswers] =
    State.inspect(_.answers)

  def setUserAnswerTo[A](page: Page with Settable[A], answer: A)(implicit writes: Writes[A]): JourneyStep[Unit] =
    State.modify { journeyState =>
      journeyState.copy(answers = journeyState.answers.set(page, answer).success.value)
    }

  def remove[A](settable: Settable[A]): JourneyStep[Unit] =
    State.modify { journeyState =>
      journeyState.copy(answers = journeyState.answers.remove(settable).success.value)
    }

  def pageMustBe(expectedPage: Page)(implicit position: Position): JourneyStep[Unit] =
    getPage.map { page =>
      page mustEqual expectedPage
    }

  def waypointsMustBe(expectedWaypoints: Waypoints)(implicit position: Position): JourneyStep[Unit] =
    getWaypoints.map { waypoints =>
      waypoints mustEqual expectedWaypoints
    }

  def answersMustContain[A](gettable: Gettable[A])(implicit reads: Reads[A], position: Position): JourneyStep[Unit] =
    getAnswers.map { answers =>
      answers.get(gettable) mustBe defined
    }

  def answersMustNotContain[A](gettable: Gettable[A])(implicit reads: Reads[A], position: Position): JourneyStep[Unit] =
    getAnswers.map { answers =>
      answers.get(gettable) must not be defined
    }

  def answerMustEqual[A](gettable: Gettable[A], expectedAnswer: A)(implicit reads: Reads[A], position: Position): JourneyStep[Unit] =
    getAnswers.map { answers =>
      answers.get(gettable).value mustEqual expectedAnswer
    }

  def submitAnswer[A](page: Page with Settable[A], value: A)(implicit writes: Writes[A], position: Position): JourneyStep[Unit] = {
    for {
      _               <- pageMustBe(page)
      originalAnswers <- getAnswers
      _               <- setUserAnswerTo(page, value)
      _               <- next(originalAnswers)
    } yield ()
  }

  def removeAddToListItem[A](settable: Settable[A])(implicit writes: Writes[A], position: Position): JourneyStep[Unit] = {
    for {
      originalAnswers <- getAnswers
      _               <- remove(settable)
      _               <- next(originalAnswers)
    } yield ()
  }

  def goTo(page: Page): JourneyStep[Unit] =
    State.modify(_.copy(page = page))

  def goToChangeAnswer(page: Page, sourcePage: WaypointPage): JourneyStep[Unit] =
    State.modify { journeyState =>
      val PageAndWaypoints(nextPage, waypoints) = page.changeLink(journeyState.waypoints, sourcePage)
      journeyState.copy(page = nextPage, waypoints = waypoints)
    }

  def goToChangeAnswer(page: Page): JourneyStep[Unit] =
    for {
      currentPage <- getPage
      _ <- goToChangeAnswer(page, currentPage.asInstanceOf[WaypointPage])
    } yield ()
}
