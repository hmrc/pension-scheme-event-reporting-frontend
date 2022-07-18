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

package models.enumeration.binders

import play.api.mvc.PathBindable

object EnumPathBinder {

  def pathBinder[T <: Enumeration](enum: T)(implicit stringBinder: PathBindable[String]): PathBindable[T#Value] = new PathBindable[T#Value] {

    def bind(key: String, value: String): Either[String, T#Value] = {
      enumtByName(enum, value) match {
        case Some(v) => Right(v)
        case None => Left(s"Unknown Enum Type $value")
      }
    }

    override def unbind(key: String, value: T#Value): String = stringBinder.unbind(key, value.toString)

    private def enumtByName(enum: T, key: String): Option[T#Value] = enum.values.find(_.toString == key)
  }
}
