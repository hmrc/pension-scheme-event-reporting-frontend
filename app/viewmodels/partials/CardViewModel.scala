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

package viewmodels.partials

import play.api.libs.json.{Format, Json}

case class CardSubHeadingParam(
                                subHeadingParam: String,
                                subHeadingParamClasses: String
                              )

object CardSubHeadingParam {
  implicit val format: Format[CardSubHeadingParam] = Json.format[CardSubHeadingParam]
}

case class CardSubHeading(
                           subHeading: String,
                           subHeadingClasses: String,
                           subHeadingParams: Seq[CardSubHeadingParam] = Seq.empty
                         )

object CardSubHeading {
  implicit val format: Format[CardSubHeading] = Json.format[CardSubHeading]
}

case class CardViewModel(
                          id: String,
                          heading: String,
                          subHeadings: Seq[CardSubHeading] = Seq.empty,
                          links: Seq[Link] = Nil
                        )
