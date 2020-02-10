package org.davidbild.tristate.contrib.scalaz

import org.davidbild.tristate.{Tristate, SpecLite}

import org.scalacheck.Arbitrary
import org.scalacheck.Gen.{const, frequency, sized}

import scalaz.std.AllInstances._
import scalaz.scalacheck.ScalazProperties._

object TristateTests extends SpecLite {

  import Tristate._

  private implicit def arbTristate[A: Arbitrary]: Arbitrary[Tristate[A]] = {
    val arb = implicitly[Arbitrary[A]]
    Arbitrary(sized(n =>
      frequency(
        (1, arb.arbitrary.map(Present(_))),
        (1, const(Absent)),
        (1, const(Unspecified))
      )
    ))
  }

  private implicit val arbTristateCobindInt: Arbitrary[Tristate[Int] => Int] =
    Arbitrary((x: Tristate[Int]) => x match {
      case Present(x) => x
      case _ => 0
    })

  checkAll("Tristate", order.laws[Tristate[String]])
  checkAll("Tristate", equal.laws[Tristate[String]])
  checkAll("Tristate", monoid.laws[Tristate[String]])
  checkAll("Tristate", monadPlus.laws[Tristate])
  checkAll("Tristate", traverse.laws[Tristate])
  checkAll("Tristate", isEmpty.laws[Tristate])
  checkAll("Tristate", cobind.laws[Tristate])

}
