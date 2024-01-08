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

package utils

import models.Index
import models.enumeration.EventType
import models.enumeration.EventType._
import models.requests.DataRequest
import play.api.mvc.{AnyContent, Result}
import services.CompileService
import uk.gov.hmrc.http.HeaderCarrier
import utils.event1.Event1UserAnswerValidation
import utils.event10.Event10UserAnswerValidation
import utils.event11.Event11UserAnswerValidation
import utils.event12.Event12UserAnswerValidation
import utils.event13.Event13UserAnswerValidation
import utils.event14.Event14UserAnswerValidation
import utils.event19.Event19UserAnswerValidation
import utils.event2.Event2UserAnswerValidation
import utils.event20.Event20UserAnswerValidation
import utils.event22and23.Events22and23UserAnswerValidation
import utils.event24.Event24UserAnswerValidation
import utils.event3.Event3UserAnswerValidation
import utils.event4.Event4UserAnswerValidation
import utils.event5.Event5UserAnswerValidation
import utils.event6.Event6UserAnswerValidation
import utils.event7.Event7UserAnswerValidation
import utils.event8.Event8UserAnswerValidation
import utils.event8A.Event8AUserAnswerValidation

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class UserAnswersValidation @Inject()(compileService: CompileService,
                                           event1UserAnswerValidation: Event1UserAnswerValidation,
                                           event2UserAnswerValidation: Event2UserAnswerValidation,
                                           event3UserAnswerValidation: Event3UserAnswerValidation,
                                           event4UserAnswerValidation: Event4UserAnswerValidation,
                                           event5UserAnswerValidation: Event5UserAnswerValidation,
                                           event6UserAnswerValidation: Event6UserAnswerValidation,
                                           event7UserAnswerValidation: Event7UserAnswerValidation,
                                           event8UserAnswerValidation: Event8UserAnswerValidation,
                                           event8AUserAnswerValidation: Event8AUserAnswerValidation,
                                           event10UserAnswerValidation: Event10UserAnswerValidation,
                                           event11UserAnswerValidation: Event11UserAnswerValidation,
                                           event12UserAnswerValidation: Event12UserAnswerValidation,
                                           event13UserAnswerValidation: Event13UserAnswerValidation,
                                           event14UserAnswerValidation: Event14UserAnswerValidation,
                                           event19UserAnswerValidation: Event19UserAnswerValidation,
                                           event20UserAnswerValidation: Event20UserAnswerValidation,
                                           events22and23UserAnswerValidation: Events22and23UserAnswerValidation,
                                           event24UserAnswerValidation: Event24UserAnswerValidation) {

  def validate(eventType: EventType, index: Index = Index(0))
              (implicit hc: HeaderCarrier, executor: ExecutionContext, request: DataRequest[AnyContent]): Future[Result] = {
    eventType match {
      case Event1 => event1UserAnswerValidation.validateAnswers(index)
      case Event2 => event2UserAnswerValidation.validateAnswers(index)
      case Event3 => event3UserAnswerValidation.validateAnswers(index)
      case Event4 => event4UserAnswerValidation.validateAnswers(index)
      case Event5 => event5UserAnswerValidation.validateAnswers(index)
      case Event6 => event6UserAnswerValidation.validateAnswers(index)
      case Event7 => event7UserAnswerValidation.validateAnswers(index)
      case Event8 => event8UserAnswerValidation.validateAnswers(index)
      case Event8A => event8AUserAnswerValidation.validateAnswers(index)
      case Event10 => event10UserAnswerValidation.validateAnswers
      case Event11 => event11UserAnswerValidation.validateAnswers
      case Event12 => event12UserAnswerValidation.validateAnswers
      case Event13 => event13UserAnswerValidation.validateAnswers
      case Event14 => event14UserAnswerValidation.validateAnswers
      case Event19 => event19UserAnswerValidation.validateAnswers
      case Event20 => event20UserAnswerValidation.validateAnswers
      case Event22 => events22and23UserAnswerValidation.validateAnswers(index, Event22)
      case Event23 => events22and23UserAnswerValidation.validateAnswers(index, Event23)
      case _ => event24UserAnswerValidation.validateAnswers(index)
    }
  }
}
