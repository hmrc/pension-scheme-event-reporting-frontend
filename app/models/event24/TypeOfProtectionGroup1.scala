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

package models.event24

import models.{Enumerable, WithName}

sealed trait TypeOfProtectionGroup1

object TypeOfProtectionGroup1 extends Enumerable.Implicits {

  case object NonResidenceEnhancement extends WithName("nonResidenceEnhancement") with TypeOfProtectionGroup1

  case object PensionCreditsPreCRE extends WithName("pensionCreditsPreCRE") with TypeOfProtectionGroup1

  case object PreCommencement extends WithName("preCommencement") with TypeOfProtectionGroup1

  case object RecognisedOverseasPSTE extends WithName("recognisedOverseasPSTE") with TypeOfProtectionGroup1

  case object SchemeSpecific extends WithName("schemeSpecific") with TypeOfProtectionGroup1

  case object NoneOfTheAbove extends WithName("noneOfTheAbove") with TypeOfProtectionGroup1

  val values: Seq[TypeOfProtectionGroup1] = Seq(NonResidenceEnhancement,
    PensionCreditsPreCRE, PreCommencement, RecognisedOverseasPSTE, SchemeSpecific, NoneOfTheAbove)

  val protectionOptions: Seq[TypeOfProtectionGroup1] = values.filter(_ != NoneOfTheAbove)

  implicit val enumerable: Enumerable[TypeOfProtectionGroup1] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
