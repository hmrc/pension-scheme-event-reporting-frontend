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

package utils.event20A

import models.enumeration.AdministratorOrPractitioner.{Administrator, Practitioner}
import models.event20A.WhatChange
import models.requests.DataRequest
import pages.EmptyWaypoints
import pages.event20A.{BecameDatePage, CeasedDatePage, Event20ACheckYourAnswersPage, WhatChangePage}
import play.api.mvc.AnyContent
import services.CompileService

import javax.inject.Inject

class Event20AUserAnswerValidation @Inject()(compileService: CompileService){
  def validateAnswers()(implicit request: DataRequest[AnyContent]): String = {
    val whatChangeAnswer = request.userAnswers.get(WhatChangePage)
    val becameDateAnswer = request.userAnswers.get(BecameDatePage)
    val ceasedDateAnswer = request.userAnswers.get(CeasedDatePage)

    (whatChangeAnswer, becameDateAnswer, ceasedDateAnswer) match {
      case (Some(WhatChange.BecameMasterTrust), Some(_), _) |
           (Some(WhatChange.CeasedMasterTrust), _, Some(_)) => {
        request.loggedInUser.administratorOrPractitioner match {
          case Administrator => controllers.event20A.routes.Event20APsaDeclarationController.onPageLoad(EmptyWaypoints).url
          case Practitioner => controllers.event20A.routes.Event20APspDeclarationController.onPageLoad(EmptyWaypoints).url
        }
      }
      case (Some(WhatChange.BecameMasterTrust), None, _) => BecameDatePage.changeLink(EmptyWaypoints, Event20ACheckYourAnswersPage()).url
      case (Some(WhatChange.CeasedMasterTrust), _, None) => CeasedDatePage.changeLink (EmptyWaypoints, Event20ACheckYourAnswersPage()).url
      case _ => WhatChangePage.changeLink(EmptyWaypoints, Event20ACheckYourAnswersPage()).url
    }
  }
}
