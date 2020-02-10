import sbt._
import Keys._

object Dependencies {
  val resolvers = Seq(
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
  )

  // Versions
  object V {
    val scalaz     = (sv: String) => "7.2.30"
    val cats       = (sv: String) => "2.0.0"
    val play       = (sv: String) => Map("2.11" -> "2.5.16",
                                         "2.12" -> "2.6.3",
                                         "2.13" -> "2.8.1")(key(sv))

    val scalaCheck          =    (sv: String) => Map("2.11" -> "1.13.4",
                                                  "2.12" -> "1.14.1",
                                                  "2.13" -> "1.14.1")(key(sv))
    val scalaTest           =    (sv: String) => Map("2.11" -> "3.0.3",
                                                    "2.12" -> "3.0.8",
                                                    "2.13" -> "3.0.8")(key(sv))
    val specs2              =    (sv: String) => "4.8.3"
    val scalazScalaCheck    =    (sv: String) => "7.3.0-M32"

    val disciplineScalaTest =    (vs: String) => "1.0.0"

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

    val playJson            =  (sv: String) => "com.typesafe.play"  %% "play-json"                 % V.play(sv)

    val scalaCheck          =  (sv: String) => "org.scalacheck"     %% "scalacheck"                % V.scalaCheck(sv)          % "test"
    val scalaTest           =  (sv: String) => "org.scalatest"      %% "scalatest"                 % V.scalaTest(sv)           % "test"
    val specs2              =  (sv: String) => "org.specs2"         %% "specs2-core"               % V.specs2(sv)              % "test"

    def at(sv: String)(libs: String => ModuleID*) =
      libs map { _(sv) }
  }

}
