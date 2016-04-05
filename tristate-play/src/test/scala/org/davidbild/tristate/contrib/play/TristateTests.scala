package org.davidbild.tristate.contrib.play

import org.scalacheck.Arbitrary
import org.scalacheck.Gen.{const, sized, frequency}
import org.scalacheck.Prop._

import play.api.libs.functional.syntax._
import play.api.libs.json._

import org.davidbild.tristate.{SpecLite, Tristate}

object TristateJsonSpec extends SpecLite {
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

  case class UserPatch(title: Tristate[String], suffix: Tristate[String])

  implicit val UserFormat: Format[UserPatch] =
    ((__ \ "title").formatTristate[String] and
     (__ \ "suffix").formatTristate[String]
    )(UserPatch, unlift(UserPatch.unapply))

  "roundtrip" ! forAll { (title: Tristate[String], suffix: Tristate[String]) => {
    val patch = UserPatch(title, suffix)
    UserFormat.reads(UserFormat.writes(patch)) must_== JsSuccess(patch)
  }}

}
