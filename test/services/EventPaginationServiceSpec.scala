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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import viewmodels.SummaryListRowWithTwoValues
import services.EventPaginationService._

class EventPaginationServiceSpec extends AnyFreeSpec with Matchers {

  import EventPaginationServiceSpec._
  val fakeEventPaginationService = new EventPaginationService


  "paginatedMappedMembers" - {

    "must yield two pages for 26 mappedMembers" in {
      val expected = fakePaginationStats.totalNumberOfPages
      val actual = fakeEventPaginationService.paginateMappedMembers(fake26MappedMembers, 2).totalNumberOfPages
      expected mustEqual actual
    }


  }

  "pageStartAndEnd" - {
    "must yield (1, 25) for fake data, page 1" in {
      val expected = (1, 25)
      val actual = fakeEventPaginationService.pageStartAndEnd(1, 26, 25)
      expected mustEqual actual
    }

    "must yield (26, 26) for fake data, page 2" in {
      val expected = (26, 26)
      val actual = fakeEventPaginationService.pageStartAndEnd(2, 26, 25)
      expected mustEqual actual
    }

    "must yield (51, 74) for fake data, page 3" in {
      val expected = (51, 74)
      val actual = fakeEventPaginationService.pageStartAndEnd(3, 74, 25)
      expected mustEqual actual
    }
  }

  "pagerSeq" - {
    "must yield range of 1 to 2 for 2 pages" in {
      val expected = 1 to 2
      val actual = fakeEventPaginationService.pagerSeq(1, 2)
      expected mustEqual actual
    }
  }

}

object EventPaginationServiceSpec {

  //scalastyle:off
  val fake26MappedMembers: Seq[SummaryListRowWithTwoValues] = for {
    i <- 1 to 26
  } yield {
    SummaryListRowWithTwoValues(s"$i", "First value", "Second value", None)
  }
  val fakePaginationStats: PaginationStats = PaginationStats(fake26MappedMembers, 26, 2, (1, 2), Seq(1, 2))

}
