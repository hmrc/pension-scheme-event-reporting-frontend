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

import com.typesafe.config.ConfigException
import config.FrontendAppConfig
import org.apache.commons.lang3.StringUtils
import play.api.Environment
import play.api.libs.json.{JsBoolean, Json}

import javax.inject.{Inject, Singleton}

@Singleton
class CountryOptionsUKEUOrEEA(val options: Seq[InputOption]) {

  @Inject()
  def this(environment: Environment, config: FrontendAppConfig) = {
    this(
      environment.resourceAsStream(config.locationCanonicalListEUAndEEA).flatMap {
        in =>
          val locationJsValue = Json.parse(in)
          Json.fromJson[Seq[Seq[String]]](locationJsValue).asOpt.map {
            _.map { countryList =>
              InputOption(countryList(1).replaceAll("country:", StringUtils.EMPTY), countryList.head)
            }
          }
      }.getOrElse {
        throw new ConfigException.BadValue(config.locationCanonicalListEUAndEEA, "country json does not exist")
      }
    )
  }

  val isCountryUKEUorEEA: String => JsBoolean = (countryCode: String) => {
    options.collectFirst {
      case country if country.value == countryCode => JsBoolean(true)
    }.getOrElse(JsBoolean(false))
  }
}
