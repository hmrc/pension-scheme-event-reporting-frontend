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

import viewmodels.{SummaryListRowWithThreeValues, SummaryListRowWithTwoValues}

import javax.inject.Singleton

@Singleton
class EventPaginationService {

  import EventPaginationService._


  def paginateMappedMembers(mappedMembers: Seq[SummaryListRowWithTwoValues], pageNumber: Int): PaginationStats = {
    val totalNumberOfMembers = mappedMembers.length
    val totNumOfPages = totalNumberOfPages(totalNumberOfMembers, pageSize)
    val pagStartAndEnd = pageStartAndEnd(pageNumber, totalNumberOfMembers, pageSize)
    val pagerSeqForNav = pagerSeq(pageNumber, totNumOfPages)
    PaginationStats(
      mappedMembers.slice(pagStartAndEnd._1 - 1, pagStartAndEnd._2),
      totalNumberOfMembers,
      totNumOfPages,
      pagStartAndEnd,
      pagerSeqForNav
    )
  }

  def paginateMappedMembersThreeValues(mappedMembers: Seq[SummaryListRowWithThreeValues], pageNumber: Int): PaginationStatsEvent7 = {
    val totalNumberOfMembers = mappedMembers.length
    val totNumOfPages = totalNumberOfPages(totalNumberOfMembers, pageSize)
    val pagStartAndEnd = pageStartAndEnd(pageNumber, totalNumberOfMembers, pageSize)
    val pagerSeqForNav = pagerSeq(pageNumber, totNumOfPages)

    PaginationStatsEvent7(
      mappedMembers.slice(pagStartAndEnd._1 - 1, pagStartAndEnd._2),
      totalNumberOfMembers,
      totNumOfPages,
      pagStartAndEnd,
      pagerSeqForNav
    )
  }

  def totalNumberOfPages(totalNumberOfMembers: Int, pageSize: Int): Int = (totalNumberOfMembers.toFloat / pageSize).ceil.toInt

  def pageStartAndEnd(pageNumber: Int, totalNumberOfMembers: Int, pageSize: Int): (Int, Int) = {
    val pagNum = pageNumber + 1
    val start = if (pagNum == 1) {
      1
    } else {
      ((pagNum - 1) * pageSize) + 1
    }
    val end = if (pagNum == 0) {
      if (totalNumberOfMembers >= pageSize) {
        pageSize
      } else {
        totalNumberOfMembers
      }
    } else if (pagNum * pageSize >= totalNumberOfMembers) {
      totalNumberOfMembers
    }
     else {
      pagNum * pageSize
    }
    if (start > end) {
      (end, end)
    } else {
      (start, end)
    }
  }

  def pagerSeq(pageNumber: Int, totalNumberOfPages: Int): Seq[Int] = {
    (pageNumber, totalNumberOfPages) match {
      case (_, tnop) if tnop <= 5 => 1 to tnop
      case (pn, _) if pn < 3 => 1 to 5
      case (pn, tnop) if pn > (tnop - 3) => (tnop - 4) to tnop
      case (pn, _) => (pn - 2) to (pn + 2)
    }
  }

}

object EventPaginationService {

  case class PaginationStatsEvent7(
                              slicedMembers: Seq[SummaryListRowWithThreeValues],
                              totalNumberOfMembers: Int,
                              totalNumberOfPages: Int,
                              pageStartAndEnd: (Int, Int),
                              pagerSeq: Seq[Int]
                            )

  case class PaginationStats(
                               slicedMembers: Seq[SummaryListRowWithTwoValues],
                               totalNumberOfMembers: Int,
                               totalNumberOfPages: Int,
                               pageStartAndEnd: (Int, Int),
                               pagerSeq: Seq[Int]
                             )
  private val pageSize = 25
}
