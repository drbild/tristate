package org.davidbild.tristate

import org.scalacheck.Arbitrary
import org.scalacheck.Gen.{const, sized, frequency}
import org.scalacheck.Prop._

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

  "present preserved through Option" ! forAll { (x: Tristate[Int]) => !x.isUnspecified ==>
    propBoolean(Tristate.fromOption(x.toOption) == x)
  }

  "present and absent preserved through specify" ! forAll { (x: Tristate[Int], y: Option[Int]) => !x.isUnspecified ==>
    propBoolean(Tristate.fromOption(x specify y) == x)
  }

  "unspecified specifies to default" ! forAll { (x: Option[Int]) => unspecified specify x must_== x }

  "present is present" ! forAll { (x: Int) => present(x).isPresent }

  "present isn't absent" ! forAll { (x: Int) => !present(x).isAbsent }

  "present isn't unspecified" ! forAll { (x: Int) => !present(x).isUnspecified }

  "absent is absent" ! check(absent.isAbsent)

  "absent isn't present" ! check(!absent.isPresent)

  "absent isn't unspecified" ! check(!absent.isUnspecified)

  "unspecified is unspecified" ! check(unspecified.isUnspecified)

  "unspecified isn't present" ! check(!unspecified.isPresent)

  "unspecified isn't absent" ! check (!unspecified.isAbsent)

  "present to option is Some" ! forAll { (x: Int) => present(x).toOption.contains(x) }

  "absent or unspecified to option is None" ! forAll { (x: Tristate[Int]) => !x.isPresent ==>
    propBoolean(x.toOption.isEmpty)
  }

  "present to list is non-empty" ! forAll { (x: Int) => present(x).toList == List(x) }

  "absent or unspecified to list is empty" ! forAll { (x: Tristate[Int]) => !x.isPresent ==>
    propBoolean(x.toList == Nil)
  }

  "present orElse is present" ! forAll { (x: Int, t: Tristate[Int]) => present(x).orElse(t).isPresent }

}
