/*
 * Copyright 2016 David R. Bild
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

package org.davidbild.tristate.contrib.play

import org.davidbild.tristate.Tristate
import org.davidbild.tristate.Tristate.{Absent, Present, Unspecified}
import play.api.libs.json.*

final class JsPathOps(self: JsPath) {

  def readTristate[T](implicit r: Reads[T]): Reads[Tristate[T]] =
    TristateReads.tristate[T](self)(r)

  def lazyReadTristate[T](r: => Reads[T]): Reads[Tristate[T]] =
    Reads(js => TristateReads.tristate[T](self)(r).reads(js))

  def writeTristate[T](implicit w: Writes[T]): OWrites[Tristate[T]] =
    TristateWrites.tristate[T](self)(w)

  def lazyWriteTristate[T](w: => Writes[T]): OWrites[Tristate[T]] =
    OWrites((t: Tristate[T]) => TristateWrites.tristate[T](self)(w).writes(t))

  def formatTristate[T](implicit f: Format[T]): OFormat[Tristate[T]] =
    TristateFormat.tristate[T](self)(f)

  def lazyFormatTristate[T](f: => Format[T]): OFormat[Tristate[T]] =
    OFormat[Tristate[T]](r = lazyReadTristate(f), w = lazyWriteTristate(f))
}

trait ToJsPathOps {
  implicit def ToJsPathOpsFromJsPath(path: JsPath): JsPathOps = new JsPathOps(path)
}

object TristateReads extends TristateReads
object TristateWrites extends TristateWrites
object TristateFormat extends TristateFormat

trait TristateFormat {
  def tristate[A](path: JsPath)(implicit f: Format[A]): OFormat[Tristate[A]] =
    OFormat(TristateReads.tristate(path)(f), TristateWrites.tristate(path)(f))
}

trait TristateReads {
  def tristate[A](path: JsPath)(implicit reads: Reads[A]) = Reads[Tristate[A]] { json =>
    path.applyTillLast(json).fold(
      jserr => jserr,
      jsres => jsres.fold(
        _ => JsSuccess(Unspecified),
        {
          case JsNull => JsSuccess(Absent)
          case js => reads.reads(js).repath(path).map(Present(_))
        }
      )
    )
  }
}

trait TristateWrites {
  def tristate[A](path: JsPath)(implicit wrs: Writes[A]): OWrites[Tristate[A]] =
    OWrites[Tristate[A]] {
        case Present(a)  => JsPath.createObj(path -> wrs.writes(a))
        case Absent      => JsPath.createObj(path -> JsNull)
        case Unspecified => Json.obj()
      }
}
