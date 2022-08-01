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

package pages

import models.NormalMode
import models.enumeration.EventType
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.JsPath
import play.api.mvc.{Call, QueryStringBindable}

class WaypointsSpec extends AnyFreeSpec with Matchers with OptionValues with EitherValues {

  "EmptyWaypoints" - {

    ".setNextWaypoint" - {

      "must return Waypoints with this waypoint at the head" in new Fixture {

        EmptyWaypoints.setNextWaypoint(testWaypoint1) mustEqual Waypoints(List(testWaypoint1))
      }
    }

    ".recalibrate" - {

      "must return EmptyWaypoints when going to and from any kind page" in new Fixture {

        EmptyWaypoints.recalibrate(RegularPage1, RegularPage2)                     mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(RegularPage1, AddToListSection1Page1)           mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(RegularPage1, AddItemPage1)                     mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(RegularPage1, CheckAnswersPage1)                mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(AddToListSection1Page1, RegularPage2)           mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(AddToListSection1Page1, AddToListSection1Page1) mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(AddToListSection1Page1, AddToListSection2Page1) mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(AddToListSection1Page1, AddItemPage1)           mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(AddToListSection1Page1, CheckAnswersPage1)      mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(AddItemPage1, RegularPage2)                     mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(AddItemPage1, AddToListSection1Page2)           mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(AddItemPage1, AddToListSection2Page2)           mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(AddItemPage1, AddItemPage2)                     mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(AddItemPage1, CheckAnswersPage1)                mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(CheckAnswersPage1, RegularPage2)                mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(CheckAnswersPage1, AddToListSection1Page1)      mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(CheckAnswersPage1, AddItemPage1)                mustEqual EmptyWaypoints
        EmptyWaypoints.recalibrate(CheckAnswersPage1, CheckAnswersPage2)           mustEqual EmptyWaypoints
      }
    }
  }

  "NonEmptyWaypoints" - {

    ".setNextWaypoint" - {

      "must add the waypoint to the head of the list" - {

        "when the list is not empty and does not start with the new waypoint" in new Fixture {

          Waypoints(List(testWaypoint1)).setNextWaypoint(testWaypoint2) mustEqual Waypoints(List(testWaypoint2, testWaypoint1))
        }
      }

      "must return the original waypoints" - {

        "when the list starts with the new waypoint" in new Fixture {

          Waypoints(List(testWaypoint1, testWaypoint2)).setNextWaypoint(testWaypoint1) mustEqual Waypoints(List(testWaypoint1, testWaypoint2))
        }
      }
    }

    ".recalibrate" - {

      "when moving to an add-to-list question page from another in the same section" - {

        "must return the original waypoints" in new Fixture {

          val waypoints = Waypoints(List(testWaypoint1))

          waypoints.recalibrate(AddToListSection1Page1, AddToListSection1Page2) mustEqual waypoints
        }
      }

      "when moving to an add-to-list question page from any page except another in the same section" - {

        "when the original waypoints start with the target page's add item waypoint" - {

          "must return the original waypoints" in new Fixture {

            val waypoints = Waypoints(List(AddToListSection1Page1.addItemWaypoint, testWaypoint1))

            waypoints.recalibrate(RegularPage1, AddToListSection1Page1) mustEqual waypoints
            waypoints.recalibrate(AddToListSection2Page1, AddToListSection1Page1) mustEqual waypoints
            waypoints.recalibrate(CheckAnswersPage1, AddToListSection1Page1) mustEqual waypoints
            waypoints.recalibrate(AddItemPage1, AddToListSection1Page1) mustEqual waypoints
          }
        }

        "when the original waypoints do not start with the target page's add item waypoint" - {

          "must add the add-to-list question page's add item waypoint to the list" in new Fixture {

            val waypoints         = Waypoints(List(testWaypoint1))
            val expectedWaypoints = Waypoints(List(AddToListSection1Page1.addItemWaypoint, testWaypoint1))

            waypoints.recalibrate(RegularPage1, AddToListSection1Page1) mustEqual expectedWaypoints
            waypoints.recalibrate(AddToListSection2Page1, AddToListSection1Page1) mustEqual expectedWaypoints
            waypoints.recalibrate(CheckAnswersPage1, AddToListSection1Page1) mustEqual expectedWaypoints
            waypoints.recalibrate(AddItemPage1, AddToListSection1Page1) mustEqual expectedWaypoints
          }
        }
      }

      "when moving to any page except an add-to-list question page from any page" - {

        "when the target page is the only waypoint" - {

          "must return empty waypoints" in new Fixture {

            val waypoints = Waypoints(List(CheckAnswersPage1.waypoint))

            waypoints.recalibrate(RegularPage1, CheckAnswersPage1) mustEqual EmptyWaypoints
            waypoints.recalibrate(CheckAnswersPage2, CheckAnswersPage1) mustEqual EmptyWaypoints
            waypoints.recalibrate(AddToListSection1Page1, CheckAnswersPage1) mustEqual EmptyWaypoints
            waypoints.recalibrate(AddItemPage1, CheckAnswersPage1) mustEqual EmptyWaypoints
          }
        }

        "when the target page is the first of multiple waypoints" - {

          "must return the waypoints with first one removed" in new Fixture {

            val waypoints         = Waypoints(List(CheckAnswersPage1.waypoint, CheckAnswersPage2.waypoint))
            val expectedWaypoints = Waypoints(List(CheckAnswersPage2.waypoint))

            waypoints.recalibrate(RegularPage1, CheckAnswersPage1) mustEqual expectedWaypoints
            waypoints.recalibrate(CheckAnswersPage2, CheckAnswersPage1) mustEqual expectedWaypoints
            waypoints.recalibrate(AddToListSection1Page1, CheckAnswersPage1) mustEqual expectedWaypoints
            waypoints.recalibrate(AddItemPage1, CheckAnswersPage1) mustEqual expectedWaypoints
          }
        }

        "when the target page is not the first waypoint" - {

          "must return the original waypoints" in new Fixture {

            val waypoints = Waypoints(List(CheckAnswersPage1.waypoint))

            waypoints.recalibrate(RegularPage1, CheckAnswersPage2) mustEqual waypoints
            waypoints.recalibrate(CheckAnswersPage2, CheckAnswersPage2) mustEqual waypoints
            waypoints.recalibrate(AddToListSection1Page1, CheckAnswersPage2) mustEqual waypoints
            waypoints.recalibrate(AddItemPage1, CheckAnswersPage2) mustEqual waypoints
          }
        }
      }
    }

    ".fromString" - {

      "must read from a real waypoint" in new Fixture {

        val waypoint = CheckYourAnswersPage(EventType.Event18).waypoint

        Waypoints.fromString(waypoint.urlFragment).value mustEqual Waypoints(List(waypoint))
      }

      "must not read from an invalid string" in {

        Waypoints.fromString("invalid") must not be defined
      }
    }

    "must bind from a query string" in new Fixture {

      val bindable = implicitly[QueryStringBindable[Waypoints]]

      val waypoint = CheckYourAnswersPage(EventType.Event18).waypoint

      val data = Map("key" -> List(s"${waypoint.urlFragment}"))
      val expectedResult = Waypoints(List(waypoint))

      bindable.bind("key", data).value.value mustEqual expectedResult
    }
  }

  private class Fixture {

    object RegularPage1 extends Page {
      override def route(waypoints: Waypoints): Call = Call("", "")
    }

    object RegularPage2 extends Page {
      override def route(waypoints: Waypoints): Call = Call("", "")
    }

    object CheckAnswersPage1 extends CheckAnswersPage {
      override val urlFragment: String = "check-1"

      override def route(waypoints: Waypoints): Call = Call("", "")
    }

    object CheckAnswersPage2 extends CheckAnswersPage {
      override val urlFragment: String = "check-2"

      override def route(waypoints: Waypoints): Call = Call("", "")
    }

    object Section1 extends AddToListSection
    object Section2 extends AddToListSection

    object AddItemPage1 extends AddItemPage {
      override val normalModeUrlFragment: String = "add-page-1"
      override val checkModeUrlFragment: String = "change-page-1"

      override def route(waypoints: Waypoints): Call = Call("", "")
    }

    object AddItemPage2 extends AddItemPage {
      override val normalModeUrlFragment: String = "add-page-2"
      override val checkModeUrlFragment: String = "change-page-2"

      override def route(waypoints: Waypoints): Call = Call("", "")
    }

    trait AddToListSection1Page extends QuestionPage[Nothing] with AddToListQuestionPage {
      override val section: AddToListSection = Section1
      override val addItemWaypoint: Waypoint = AddItemPage1.waypoint(NormalMode)

      override def path: JsPath = ???
    }

    trait AddToListSection2Page extends QuestionPage[Nothing] with AddToListQuestionPage {
      override val section: AddToListSection = Section2
      override val addItemWaypoint: Waypoint = AddItemPage2.waypoint(NormalMode)

      override def path: JsPath = ???
    }

    object AddToListSection1Page1 extends AddToListSection1Page {
      override def route(waypoints: Waypoints): Call = Call("", "")
    }

    object AddToListSection1Page2 extends AddToListSection1Page {
      override def route(waypoints: Waypoints): Call = Call("", "")
    }

    object AddToListSection2Page1 extends AddToListSection2Page {
      override def route(waypoints: Waypoints): Call = Call("", "")
    }

    object AddToListSection2Page2 extends AddToListSection2Page {
      override def route(waypoints: Waypoints): Call = Call("", "")
    }

    val testWaypoint1: Waypoint = CheckAnswersPage1.waypoint
    val testWaypoint2: Waypoint = CheckAnswersPage2.waypoint
  }
}
