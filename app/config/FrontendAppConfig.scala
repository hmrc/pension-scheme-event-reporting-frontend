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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {
  private def getConfigString(key: String) = servicesConfig.getConfString(key,
    throw new Exception(s"Could not find config '$key'"))

  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)
  val betaFeedbackUnauthenticatedUrl: String = getConfigString("contact-frontend.beta-feedback-url.unauthenticated")
}
