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

package org.davidbild.tristate.contrib.cats

import org.davidbild.tristate.Tristate
import org.davidbild.tristate.Tristate._

import cats._
import cats.data.{Ior, Validated, ValidatedNel}

import scala.annotation.tailrec

trait TristateInstances extends TristateInstances2 {

  implicit val tristateCatsInstancesForTristate: Traverse[Tristate] with CommutativeApplicative[Tristate] with ApplicativeError[Tristate, Unit] with Monad[Tristate] with CoflatMap[Tristate] with MonoidK[Tristate] =
    new Traverse[Tristate] with CommutativeApplicative[Tristate] with ApplicativeError[Tristate, Unit] with Monad[Tristate] with CoflatMap[Tristate] with MonoidK[Tristate] {

      def empty[A]: Tristate[A] = Absent

      def combineK[A](x: Tristate[A], y: Tristate[A]): Tristate[A] =
        x.cata(Present(_), y, y orElse Unspecified)

      def pure[A](x: A): Tristate[A] = Present(x)

      override def map[A, B](fa: Tristate[A])(f: A => B): Tristate[B] =
        fa.map(f)

      def flatMap[A, B](fa: Tristate[A])(f: A => Tristate[B]): Tristate[B] =
        fa.flatMap(f)

      @tailrec
      def tailRecM[A, B](x: A)(f: A => Tristate[Either[A, B]]): Tristate[B] =
        f(x) match {
          case Unspecified       => Unspecified
          case Absent            => Absent
          case Present(Left(a))  => tailRecM(a)(f)
          case Present(Right(b)) => Present(b)
        }

      override def map2[A, B, Z](fa: Tristate[A], fb: Tristate[B])(f: (A, B) => Z): Tristate[Z] =
        fa.flatMap(a => fb.map(b => f(a, b)))

      override def map2Eval[A, B, Z](fa: Tristate[A], fb: Eval[Tristate[B]])(f: (A, B) => Z): Eval[Tristate[Z]] =
        fa match {
          case Unspecified => Now(Unspecified)
          case Absent      => Now(Absent)
          case Present(a)  => fb.map(_.map(f(a, _)))
        }

      def coflatMap[A, B](fa: Tristate[A])(f: Tristate[A] => B): Tristate[B] =
        fa.cobind(f)

      def foldLeft[A, B](fa: Tristate[A], b: B)(f: (B, A) => B): B =
        fa.cata(f(b, _), b, b)

      def foldRight[A, B](fa: Tristate[A],lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
        fa.cata(f(_, lb), lb, lb)

      def raiseError[A](e: Unit): Tristate[A] = Absent

      def handleErrorWith[A](fa: Tristate[A])(f: (Unit) => Tristate[A]): Tristate[A] = fa orElse f(())

      def traverse[G[_]: Applicative, A, B](fa: Tristate[A])(f: A => G[B]): G[Tristate[B]] = {
        val G = Applicative[G]
        fa.cata(a => G.map(f(a))(Present(_)), G.pure(Absent), G.pure(Unspecified))
      }

      override def reduceLeftToOption[A, B](fa: Tristate[A])(f: A => B)(g: (B, A) => B): Option[B] =
        fa.map(f).toOption

      override def reduceRightToOption[A, B](fa: Tristate[A])(f: A => B)(g: (A, Eval[B]) => Eval[B]): Eval[Option[B]] =
        Now(fa.map(f).toOption)

      override def reduceLeftOption[A](fa: Tristate[A])(f: (A, A) => A): Option[A] = fa.toOption

      override def reduceRightOption[A](fa: Tristate[A])(f: (A, Eval[A]) => Eval[A]): Eval[Option[A]] =
        Now(fa.toOption)

      override def minimumOption[A](fa: Tristate[A])(implicit A: Order[A]): Option[A] = fa.toOption

      override def maximumOption[A](fa: Tristate[A])(implicit A: Order[A]): Option[A] = fa.toOption

      override def get[A](fa: Tristate[A])(idx: Long): Option[A] =
        if (idx == 0L) fa.toOption else None

      override def size[A](fa: Tristate[A]): Long = fa.cata(_ => 1L, 0L, 0L)

      override def foldMap[A, B](fa: Tristate[A])(f: A => B)(implicit B: Monoid[B]): B =
        fa.cata(f, B.empty, B.empty)

      override def find[A](fa: Tristate[A])(f: A => Boolean): Option[A] =
        fa.filter(f).toOption

      override def exists[A](fa: Tristate[A])(p: A => Boolean): Boolean =
        fa.exists(p)

      override def forall[A](fa: Tristate[A])(p: A => Boolean): Boolean =
        fa.forall(p)

      override def toList[A](fa: Tristate[A]): List[A] =
        fa.toList

      override def filter_[A](fa: Tristate[A])(p: A => Boolean): List[A] =
        fa.filter(p).toList

      override def takeWhile_[A](fa: Tristate[A])(p: A => Boolean): List[A] =
        fa.filter(p).toList

      override def dropWhile_[A](fa: Tristate[A])(p: A => Boolean): List[A] =
        fa.filterNot(p).toList

      override def isEmpty[A](fa: Tristate[A]): Boolean =
        !fa.isPresent
    }

  implicit def tristateCatsShowForTristate[A](implicit A: Show[A]): Show[Tristate[A]] =
    new TristateShow[A]
}

trait TristateInstances2 extends TristateInstances1 {
  implicit def tristateCatsOrderForTristate[A: Order]: Order[Tristate[A]] =
    new TristateOrder[A]
  implicit def tristateCatsMonoidForTristate[A: Semigroup]: Monoid[Tristate[A]] =
    new TristateMonoid[A]
}

trait TristateInstances1 extends TristateInstances0 {
  implicit def tristateCatsPartialOrderForTristate[A: PartialOrder]: PartialOrder[Tristate[A]] =
    new TristatePartialOrder[A]
}

trait TristateInstances0 {
  implicit def tristateCatsEqForTristate[A: Eq]: Eq[Tristate[A]] =
    new TristateEq[A]
}

class TristateShow[A](implicit A: Show[A]) extends Show[Tristate[A]] {
  def show(fa: Tristate[A]): String =
    fa.cata(a => s"Present(${A.show(a)})", "Absent", "Unspecified")
}

class TristateOrder[A](implicit A: Order[A]) extends Order[Tristate[A]] {
  def compare(x: Tristate[A], y: Tristate[A]): Int =
    (x, y) match {
      case (Present(a) , Present(b))  =>  A.compare(a, b)
      case (_          , Present(_))  => -1
      case (Present(_) , _)           =>  1
      case (Absent     , Absent)      =>  0
      case (Absent     , Unspecified) =>  1
      case (Unspecified, Absent)      => -1
      case (Unspecified, Unspecified) =>  0
    }
}

class TristatePartialOrder[A](implicit A: PartialOrder[A]) extends PartialOrder[Tristate[A]] {
  def partialCompare(x: Tristate[A], y: Tristate[A]): Double =
    (x, y) match {
      case (Present(a) , Present(b))  =>  A.partialCompare(a, b)
      case (_          , Present(_))  => -1.0
      case (Present(_) , _)           =>  1.0
      case (Absent     , Absent)      =>  0.0
      case (Absent     , Unspecified) =>  1.0
      case (Unspecified, Absent)      => -1.0
      case (Unspecified, Unspecified) =>  0.0
    }
}

class TristateEq[A](implicit A: Eq[A]) extends Eq[Tristate[A]] {
  def eqv(x: Tristate[A], y: Tristate[A]): Boolean =
    (x, y) match {
      case (Unspecified, Unspecified) => true
      case (Absent     , Absent)      => true
      case (Present(a) , Present(b))  => A.eqv(a, b)
      case (_          , _)           => false
    }
}

class TristateMonoid[A](implicit A: Semigroup[A]) extends Monoid[Tristate[A]] {
  def empty: Tristate[A] = Unspecified
  def combine(x: Tristate[A], y: Tristate[A]): Tristate[A] =
    (x, y) match {
      case (Unspecified, _)           => y
      case (_          , Unspecified) => x
      case (Absent     , _)           => y
      case (_          , Absent)      => x
      case (Present(a) , Present(b))  => Present(A.combine(a, b))
    }
}

trait TristateSyntax {
  implicit final def tristateCatsSyntaxTristate[A](fa: Tristate[A]): TristateOps[A] = new TristateOps(fa)
}

final class TristateOps[A](val fa: Tristate[A]) extends AnyVal {
  /**
    * If the `Tristate` is `Present`, return its value in a
    * [[cats.data.Validated.Invalid]].
    * If the `Tristate` is `Absent` or `Unspecified`, return the
    * provided `B` value in a [[cats.data.Validated.Valid]].
    */
  def toInvalid[B](b: => B): Validated[A, B] =
    fa.cata(Validated.Invalid(_), Validated.Valid(b), Validated.Valid(b))

  /**
    * If the `Tristate` is `Present`, wrap its value in a 
    * [[cats.data.NonEmptyList]] and return it in a
    * [[cats.data.Validated.Invalid]].
    * If the `Tristate` is `Absent` or `Unspecified`, return
    * the provided `B` value in a [[cats.data.Validated.Valid]].
    */
  def toInvalidNel[B](b: => B): ValidatedNel[A, B] =
    fa.cata(Validated.invalidNel(_), Validated.Valid(b), Validated.Valid(b))

  /**
   * If the `Tristate` is `Present`, return its value in a
   * [[cats.data.Validated.Valid]].
   * If the `Tristate` is `Absent` or `Unspecified`, return
   * the provided `B` value in a [[cats.data.Validated.Invalid]].
   */
  def toValid[B](b: => B): Validated[B, A] =
    fa.cata(Validated.Valid(_), Validated.Invalid(b), Validated.Invalid(b))

  /**
   * If the `Tristate` is `Present`, return its value in a
   * [[cats.data.Validated.Valid]].
   * If the `Tristate` is `Absent` or `Unspecified`, wrap
   * the provided `B` value in a [[cats.data.NonEmptyList]]
   * and return the result in a [[cats.data.Validated.Invalid]].
   */
  def toValidNel[B](b: => B): ValidatedNel[B, A] =
    fa.cata(Validated.Valid(_), Validated.invalidNel(b), Validated.invalidNel(b))

  /**
    * If the `Tristate` is `Present`, return its value in a
    * [[cats.data.Ior.Right]].
    * If the `Tristate` is `Absent` or `Unspecified`, wrap
    * the provided `B` value in a [[cats.data.Ior.Left]].
    */
  def toRightIor[B](b: => B): Ior[B, A] =
    fa.cata(Ior.Right(_), Ior.Left(b), Ior.Left(b))

  /**
    * If the `Tristate` is `Present`, return its value in a
    * [[cats.data.Ior.Left]].
    * If the `Tristate` is `Absent` or `Unspecified`, wrap
    * the provided `B` value in a [[cats.data.Ior.Right]].
    */
  def toLeftIor[B](b: => B): Ior[A, B] =
    fa.cata(Ior.Left(_), Ior.Right(b), Ior.Right(b))

  /**
   * If the `Tristate` is `Present`, return its value. If 
   * the `Tristate` is `Absent` or `Unspecified, return the
   * `empty` value for `Monoid[A]`.
   */
  def orEmpty(implicit A: Monoid[A]): A =
    fa.getOrElse(A.empty)

}
