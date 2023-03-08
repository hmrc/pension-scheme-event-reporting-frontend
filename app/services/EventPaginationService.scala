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

import viewmodels.SummaryListRowWithTwoValues

class EventPaginationService {
  import EventPaginationService._


  def paginateMappedMembers(mappedMembers: Seq[SummaryListRowWithTwoValues]): PaginatedMembers = {
    val totalNumberOfMembers = mappedMembers.length
    PaginatedMembers(totalNumberOfMembers, calculateNumberOfPaginationItems(totalNumberOfMembers))
  }

  def calculateNumberOfPaginationItems(num: Int): Int = {
    val numBy25 = (num / 25) + 1
    println("\n\n\n\n\n" + numBy25)
    numBy25 match {
      case 5 if (numBy25 >= 5) => 5
      case numBy25 if (numBy25 >= 0 & numBy25 < 5) => numBy25
      case _ => 0
    }
  }
}

object EventPaginationService {
  case class PaginatedMembers(
                               totalNumberOfMembers: Int,
                               numberOfPaginationItems: Int
                             )
}
