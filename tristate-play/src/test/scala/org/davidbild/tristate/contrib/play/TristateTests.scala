package org.davidbild.tristate.contrib.play

import org.scalacheck.Arbitrary
import org.scalacheck.Gen.{const, sized, frequency}
import org.scalacheck.Prop.*

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

import org.davidbild.tristate.{SpecLite, Tristate}

case class UserPatch(title: Tristate[String], suffix: Tristate[String])

object UserPatch {
  implicit val UserFormat: OFormat[UserPatch] =
    ((__ \ "title").formatTristate[String] and
      (__ \ "suffix").formatTristate[String]
      )(UserPatch.apply, u => (u.title, u.suffix))
}

object TristateTests extends SpecLite {

  import Tristate._
  import UserPatch.*

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

  "roundTrip" ! forAll { (title: Tristate[String], suffix: Tristate[String]) => {
    val patch = UserPatch(title, suffix)
    UserFormat.reads(UserFormat.writes(patch)) must_== JsSuccess(patch)
  }
  }

}
