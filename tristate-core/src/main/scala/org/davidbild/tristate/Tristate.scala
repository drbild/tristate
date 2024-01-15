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

package org.davidbild.tristate

/**
 * An optional value.
 *
 * A `Tristate[A]` will either be a wrapped `A` instance (`Present[A]`), an explicit lack of an underlying
 * `A` instance (`Absent`), or an implicit lack of an underlying `A` instance (`Unspecified`).
 */
sealed abstract class Tristate[+A] extends Product with Serializable {

  import Tristate.*

  final def cata[B](f: A => B, ifAbsent: => B, ifUnspecified: => B): B =
    this match {
      case Present(a) => f(a)
      case Absent => ifAbsent
      case Unspecified => ifUnspecified
    }

  /**
   * Discharges this `Tristate` to an `Option` using the specified default for the `Unspecified` case.
   * `Present(a)` goes to `Some(a)` and `Absent` goes to `None`.
   */
  final def specify[B >: A](default: => Option[B]): Option[B] =
   cata(Some(_), None, default)

  final def getOrElse[B >: A](default: => B): B =
    cata(identity, default, default)

  final def isPresent: Boolean =
    cata(_ => true, false, false)

  final def isAbsent: Boolean =
    cata(_ => false, true, false)

  final def isUnspecified: Boolean =
    cata(_ => false, false, true)

  final def map[B](f: A => B): Tristate[B] =
    cata(f andThen present, absent, unspecified)

  final def flatMap[B](f: A => Tristate[B]): Tristate[B] =
    cata(f, absent, unspecified)

  final def toOption: Option[A] =
    cata(Some(_), None, None)

  final def toList: List[A] =
    cata(List(_), Nil, Nil)

  final def orElse[B >: A](tsa: Tristate[B]): Tristate[B] =
    cata(_ => this, tsa, tsa)

  final def cojoin: Tristate[Tristate[A]] = map(present)

  final def cobind[B](f: Tristate[A] => B): Tristate[B] =
    map(_ => f(this))

  final def filter(f: A => Boolean): Tristate[A] =
    flatMap(a => if (f(a)) this else unspecified)

  final def filterNot(f: A => Boolean): Tristate[A] =
    filter(f andThen (!_))

  final def forall(f: A => Boolean): Boolean =
    cata(f, true, true)

  final def exists(f: A => Boolean): Boolean =
    cata(f, false, false)

}

object Tristate {

  final case class Present[A](a: A) extends Tristate[A]

  case object Absent extends Tristate[Nothing]

  case object Unspecified extends Tristate[Nothing]

  final def present[A](a: A): Tristate[A] = Present(a)

  final def absent[A]: Tristate[A] = Absent

  final def unspecified[A]: Tristate[A] = Unspecified

  final def fromOption[A](oa: Option[A]): Tristate[A] =
   oa.fold(absent[A])(present)

}
