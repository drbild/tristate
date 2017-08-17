package org.davidbild.tristate.contrib.cats

import cats.implicits._
import cats.laws.discipline._
import cats.kernel.laws.OrderLaws

import org.scalacheck.{Arbitrary, Cogen, Gen}
import org.scalatest.{FunSuite, Matchers}
import org.scalatest.prop._
import org.typelevel.discipline.scalatest.Discipline

import org.scalacheck.rng.Seed

import org.davidbild.tristate._
import org.davidbild.tristate.Tristate._

class TristateTests extends FunSuite with  Matchers with PropertyChecks with Discipline {

  checkAll("Tristate[Int]", CartesianTests[Tristate].cartesian[Int, Int, Int])
  checkAll("Tristate[Int] with Option", TraverseTests[Tristate].traverse[Int, Int, Int, Int, Tristate, Option])
  checkAll("Tristate with Unit", MonadErrorTests[Tristate, Unit].monadError[Int, Int, Int])
  checkAll("Tristate[Int]", ApplicativeTests[Tristate].applicative[Int, Int, Int])
  checkAll("Tristate[Int]", MonadTests[Tristate].monad[Int, Int, Int])
  checkAll("Tristate[Int]", MonoidKTests[Tristate].monoidK[Int])
  checkAll("Tristate[Int]", CoflatMapTests[Tristate].coflatMap[Int, Int, Int])

  val orderLaws = OrderLaws[Tristate[Int]]
  checkAll("Tristate[Int]", orderLaws.partialOrder)
  checkAll("Tristate[Int]", orderLaws.order)

  test("show") {
    absent[String].show should === ("Absent")
    unspecified[String].show should === ("Unspecified")

    forAll { fs: Tristate[String] =>
      fs.show should === (fs.toString)
    }
  }

  private implicit def cogenTristate[A: Cogen]: Cogen[Tristate[A]] = {
    val A = implicitly[Cogen[A]]
    Cogen((seed: Seed, t: Tristate[A]) => t.cata(a => A.perturb(seed.next, a), seed, seed))
  }

  private implicit def arbTristate[A: Arbitrary]: Arbitrary[Tristate[A]] = {
    val A = implicitly[Arbitrary[A]]
    Arbitrary(Gen.sized(n =>
      Gen.frequency(
        (1, A.arbitrary.map(Present(_))),
        (1, Gen.const(Absent)),
        (1, Gen.const(Unspecified))
      )
    ))
  }

}
