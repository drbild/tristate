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

package org.davidbild.tristate.contrib.scalaz

import org.davidbild.tristate.Tristate
import org.davidbild.tristate.Tristate._

import scalaz._
import scalaz.syntax.either._

sealed trait TristateInstances0 {
  implicit def tristateEqual[A](implicit A0: Equal[A]): Equal[Tristate[A]] = new TristateEqual[A] {
    implicit def A = A0
  }
}

trait TristateInstances extends TristateInstances0 {
  implicit val tristateInstance = new Traverse[Tristate] with MonadPlus[Tristate] with Foldable[Tristate] with Cozip[Tristate] with IsEmpty[Tristate] with Cobind[Tristate] with Optional[Tristate] {
    def point[A](a: => A) = Present(a)

    override def ap[A, B](fa: => Tristate[A])(f: => Tristate[A => B]) = f match {
      case Present(f) => fa match {
        case Present(x)  => Present(f(x))
        case Absent      => Absent
        case Unspecified => Unspecified
      }
      case Absent      => Absent
      case Unspecified => Unspecified
    }

    def bind[A, B](fa: Tristate[A])(f: A => Tristate[B]) = fa flatMap f
    override def map[A, B](fa: Tristate[A])(f: A => B) = fa map f

    def traverseImpl[F[_], A, B](fa: Tristate[A])(f: A => F[B])(implicit F: Applicative[F]) =
     fa.cata(a => F.map(f(a))(Present(_): Tristate[B]), F.point(Absent), F.point(Unspecified))

    def empty[A]: Tristate[A] = Unspecified
    def plus[A](a: Tristate[A], b: => Tristate[A]) = (a, b) match {
      case (s1 @ Present(_), Present(_))       => s1
      case (s1 @ Present(_), Absent)           => s1
      case (s1 @ Present(_), Unspecified)      => s1
      case (Absent         , s2 @ Present(_))  => s2
      case (Unspecified    , s2 @ Present(_))  => s2
      case (Absent         , Absent)           => Absent
      case (Absent         , Unspecified)      => Absent
      case (Unspecified    , Absent)           => Absent
      case (Unspecified    , Unspecified)      => Unspecified
    }

    def cozip[A, B](fa: Tristate[A \/ B]) =
     fa match {
       case Absent => -\/(Absent)
       case Unspecified => -\/(Unspecified)
       case Present(e) => e match {
         case -\/(a) => -\/(Present(a))
         case \/-(b) => \/-(Present(b))
       }
     }

    def cobind[A, B](fa: Tristate[A])(f: (Tristate[A]) => B): Tristate[B] = fa cobind f

    override def pextract[B, A](fa: Tristate[A]): \/[Tristate[B], A] =
     foldLeft[A, \/[Tristate[B], A]](fa, -\/(Unspecified))((_, x) => \/-(x))

    override def isDefined[A](fa: Tristate[A]): Boolean = fa.isPresent
    override def toOption[A](fa: Tristate[A]): Option[A] = fa.toOption
    override def getOrElse[A](fa: Tristate[A])(d: => A) = fa getOrElse d
  }

  implicit def tristateMonoid[A: Semigroup]: Monoid[Tristate[A]] = new Monoid[Tristate[A]] {
    override def zero: Tristate[A] = Unspecified

    override def append(f1: Tristate[A], f2: => Tristate[A]): Tristate[A] = (f1, f2) match {
      case (Present(a1)    , Present(a2))      => Present(Semigroup[A].append(a1, a2))
      case (s1 @ Present(_), Absent)           => s1
      case (s1 @ Present(_), Unspecified)      => s1
      case (Absent         , s2 @ Present(_))  => s2
      case (Unspecified    , s2 @ Present(_))  => s2
      case (Absent         , Absent)           => Absent
      case (Absent         , Unspecified)      => Absent
      case (Unspecified    , Absent)           => Absent
      case (Unspecified    , Unspecified)      => Unspecified
    }
  }

  implicit def tristateOrder[A](implicit A0: Order[A]): Order[Tristate[A]] = new TristateOrder[A] {
    implicit def A = A0
  }

  implicit def TristateShow[A: Show]: Show[Tristate[A]] = new Show[Tristate[A]] {
    override def show(o1: Tristate[A]) = o1 match {
      case Present(a1) => Cord("Present(") ++ Show[A].show(a1) ++ Cord(")")
      case Absent      => Cord("Absent")
      case Unspecified => Cord("Unspecified")
    }
  }

}

trait TristateFunctions {

  final def toSuccess[A, E](fa: Tristate[A])(ifAbsent: => E, ifUnspecified: => E): Validation[E, A] =
    fa.cata(Success(_), Failure(ifAbsent), Failure(ifUnspecified))

  final def toFailure[A, B](fa: Tristate[A])(ifAbsent: => B, ifUnspecified: => B): Validation[A, B] =
    fa.cata(Failure(_), Success(ifAbsent), Success(ifUnspecified))

  final def toRight[A, E](fa: Tristate[A])(ifAbsent: => E, ifUnspecified: => E): E \/ A =
     fa.cata(_.right, ifAbsent.left, ifUnspecified.left)

  final def toLeft[A, B](fa: Tristate[A])(ifAbsent: => B, ifUnspecified: => B): A \/ B =
     fa.cata(_.left, ifAbsent.right, ifUnspecified.right)

  final def toMaybe[A](fa: Tristate[A]): Maybe[A] =
     fa.cata(Maybe.just, Maybe.empty, Maybe.empty)

  final def orEmpty[A, M[_] : Applicative : PlusEmpty](fa: Tristate[A]): M[A] =
    fa.cata(Applicative[M].point(_), PlusEmpty[M].empty, PlusEmpty[M].empty)

  final def foldLift[F[_], A, B](fa: Tristate[A])(b: => B, k: F[A] => B)(implicit p: Applicative[F]): B =
    fa.cata(a => k(Applicative[F].point(a)), b, b)
}

private trait TristateEqual[A] extends Equal[Tristate[A]] {
  implicit def A: Equal[A]

  override def equalIsNatural: Boolean = A.equalIsNatural

  override def equal(f1: Tristate[A], f2: Tristate[A]): Boolean =
    (f1, f2) match {
      case (Present(a1), Present(a2)) => A.equal(a1, a2)
      case (Absent, Absent) => true
      case (Unspecified, Unspecified) => true
      case (_, _) => false
    }
}

private trait TristateOrder[A] extends Order[Tristate[A]] with TristateEqual[A] {
  implicit def A: Order[A]

  import Ordering._

  def order(f1: Tristate[A], f2: Tristate[A]) = (f1, f2) match {
    case (Present(a1), Present(a2)) => Order[A].order(a1, a2)
    case (Absent, Present(_))       => LT
    case (Unspecified, Present(_))  => LT
    case (Present(_), Absent)       => GT
    case (Present(_), Unspecified)  => GT
    case (Absent, Absent)           => EQ
    case (Absent, Unspecified)      => GT
    case (Unspecified, Absent)      => LT
    case (Unspecified, Unspecified) => EQ
  }
}
