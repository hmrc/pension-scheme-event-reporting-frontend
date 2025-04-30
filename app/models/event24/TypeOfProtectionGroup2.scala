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

sealed trait TypeOfProtectionGroup2

object TypeOfProtectionGroup2 extends Enumerable.Implicits {
  case object EnhancedProtection extends WithName("enhancedProtection") with TypeOfProtectionGroup2

  case object EnhancedProtectionWithProtectedSum extends WithName("enhancedProtectionWithProtectedSum") with TypeOfProtectionGroup2

  case object FixedProtection extends WithName("fixedProtection") with TypeOfProtectionGroup2

  case object FixedProtection2014 extends WithName("fixedProtection2014") with TypeOfProtectionGroup2

  case object FixedProtection2016 extends WithName("fixedProtection2016") with TypeOfProtectionGroup2

  case object IndividualProtection2014 extends WithName("individualProtection2014") with TypeOfProtectionGroup2

  case object IndividualProtection2016 extends WithName("individualProtection2016") with TypeOfProtectionGroup2

  case object Primary extends WithName("primary") with TypeOfProtectionGroup2

  case object PrimaryWithProtectedSum extends WithName("primaryWithProtectedSum") with TypeOfProtectionGroup2

  case object NoOtherProtections extends WithName("noOtherProtections") with TypeOfProtectionGroup2


  val values: Seq[TypeOfProtectionGroup2] = Seq(EnhancedProtection, EnhancedProtectionWithProtectedSum, FixedProtection,
    FixedProtection2014, FixedProtection2016, IndividualProtection2014, IndividualProtection2016, Primary, PrimaryWithProtectedSum, NoOtherProtections)

  val protectionOptions: Seq[TypeOfProtectionGroup2] = values.filter(_ != NoOtherProtections)

  implicit val enumerable: Enumerable[TypeOfProtectionGroup2] =
    Enumerable(values.map(v => v.toString -> v)*)
}
