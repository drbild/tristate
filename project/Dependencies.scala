import sbt._
import Keys._

object Dependencies {
  val resolvers = Seq(
    "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
  )

  // Versions
  object V {
    val scalaz     = (sv: String) => "7.3.8"
    val cats       = (sv: String) => "2.10.0"
    val play       = (sv: String) => "3.0.1"

    val scalaCheck          =    (sv: String) => "1.17.0"
    val scalaTest           =    (sv: String) => "3.2.17"
    val specs2              =    (sv: String) => "5.4.2"
    val scalazScalaCheck    =    (sv: String) => "7.3.8"

    val disciplineScalaTest =    (vs: String) => "2.2.0"

    def key(sv: String) =
      CrossVersion.partialVersion(sv) match {
        case Some((a, b)) => s"$a.$b"
        case _            => ???
      }

  }

  // Libraries
  object Libs {
    val scalaz              =  (sv: String) => "org.scalaz"         %% "scalaz-core"               % V.scalaz(sv)
    val scalazScalaCheck    =  (sv: String) => "org.scalaz"         %% "scalaz-scalacheck-binding" % V.scalazScalaCheck(sv)    % "test"

    val cats                =  (sv: String) => "org.typelevel"      %% "cats-core"                 % V.cats(sv)
    val catsLaws            =  (sv: String) => "org.typelevel"      %% "cats-laws"                 % V.cats(sv)                % "test"
    val disciplineScalaTest =  (sv: String) => "org.typelevel"      %% "discipline-scalatest"      % V.disciplineScalaTest(sv) % "test"

    val playJson            =  (sv: String) => "org.playframework"  %% "play-json"                 % V.play(sv)

    val scalaCheck          =  (sv: String) => "org.scalacheck"     %% "scalacheck"                % V.scalaCheck(sv)          % "test"
    val scalaTest           =  (sv: String) => "org.scalatest"      %% "scalatest"                 % V.scalaTest(sv)           % "test"
    val specs2              =  (sv: String) => "org.specs2"         %% "specs2-core"               % V.specs2(sv)              % "test"

    def at(sv: String)(libs: String => ModuleID*): Seq[sbt.ModuleID] =
      libs map { _(sv) }
  }

}
