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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import viewmodels.SummaryListRowWithTwoValues
import services.EventPaginationService._

//scalastyle:off magic.number
class EventPaginationServiceSpec extends AnyFreeSpec with Matchers {

  import EventPaginationServiceSpec._
  val fakeEventPaginationService = new EventPaginationService


  "paginatedMappedMembers" - {
    "must return correctly paginated data" in {
      val expected = fakePaginationStats100Members
      val actual = fakeEventPaginationService.paginateMappedMembers(fake100MappedMembers, 0)
      expected mustEqual actual
    }
  }

    "totalNumberOfPages" - {
      "must yield 1 page for 10 mapped members" in {
        val expected = 1
        val actual = fakeEventPaginationService.paginateMappedMembers(fake10MappedMembers, 0).totalNumberOfPages
        expected mustEqual actual
      }

      "must yield 2 pages for 26 mapped members" in {
        val expected = 2
        val actual = fakeEventPaginationService.paginateMappedMembers(fake26MappedMembers, 0).totalNumberOfPages
        expected mustEqual actual
      }

      "must yield 4 pages for 100 mapped members" in {
        val expected = 4
        val actual = fakeEventPaginationService.paginateMappedMembers(fake100MappedMembers, 0).totalNumberOfPages
        expected mustEqual actual
      }
    }

  "pageStartAndEnd" - {
    "must yield (1, 25) for fake data, first page" in {
      val expected = (1, 25)
      val actual = fakeEventPaginationService.pageStartAndEnd(0, 26, pageSize)
      expected mustEqual actual
    }

    "must yield (26, 26) for fake data, second page" in {
      val expected = (26, 26)
      val actual = fakeEventPaginationService.pageStartAndEnd(1, 26, pageSize)
      expected mustEqual actual
    }

    "must yield (74, 74) for fake data, fourth page (out of bounds)" in {
      val expected = (74, 74)
      val actual = fakeEventPaginationService.pageStartAndEnd(3, 74, pageSize)
      expected mustEqual actual
    }
  }

  "pagerSeq" - {

    "must yield range of 1 to 2 for 2 pages, viewed from first page" in {
      val expected = 1 to 2
      val actual = fakeEventPaginationService.pagerSeq(0, 2)
      expected mustEqual actual
    }

    "must yield range of 1 to 5 for 6 pages, viewed from first page" in {
      val expected = 1 to 5
      val actual = fakeEventPaginationService.pagerSeq(0, 6)
      expected mustEqual actual
    }

    "must yield range of 5 to 9 for 11 pages, viewed from eighth page" in {
      val expected = 5 to 9
      val actual = fakeEventPaginationService.pagerSeq(7, 11)
      expected mustEqual actual
    }

    "must yield range of 36 to 40 for 40 pages, viewed from fortieth page" in {
      val expected = 36 to 40
      val actual = fakeEventPaginationService.pagerSeq(39, 40)
      expected mustEqual actual
    }

  }

}

object EventPaginationServiceSpec {

  private val pageSize = 25

  private def fakeXMappedMembers(x: Int): Seq[SummaryListRowWithTwoValues] = for {
    i <- 1 to x
  } yield {
    SummaryListRowWithTwoValues(s"$i", "First value", "Second value", None)
  }

  private val fake10MappedMembers =  fakeXMappedMembers(10)
  private val fake26MappedMembers =  fakeXMappedMembers(26)
  private val fake100MappedMembers =  fakeXMappedMembers(100)

  private def fakePaginationStats(
                                 slicedMems: Seq[SummaryListRowWithTwoValues],
                                 totMems: Int,
                                 totPages: Int,
                                 pagStartEnd: (Int, Int),
                                 pagSeq: Seq[Int]
                                 ): PaginationStats = PaginationStats(
    slicedMembers = slicedMems,
    totalNumberOfMembers = totMems,
    totalNumberOfPages = totPages,
    pageStartAndEnd = pagStartEnd,
    pagerSeq = pagSeq
  )

  private val fakePaginationStats100Members = fakePaginationStats(
    fake100MappedMembers.slice(0, 25),
    100,
    4,
    (1, 25),
    1 to 4
  )
}
