import sbt._
import Keys._

object Dependencies {
  val resolvers = Seq(
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
  )

  // Versions
  object V {
    val scalaz     = (sv: String) => "7.2.15"
    val cats       = (sv: String) => "1.0.0-MF"
    val play       = (sv: String) => "2.5.16"

    val scalaCheck = (sv: String) => "1.12.6"
    val specs2     = (sv: String) => "3.9.4"

    def key(sv: String) =
      CrossVersion.partialVersion(sv) match {
        case Some((a, b)) => s"$a.$b"
        case _            => ???
      }

  }

  // Libraries
  object Libs {
    val scalaz           = (sv: String) => "org.scalaz"               %% "scalaz-core"               % V.scalaz(sv)
    val scalazScalaCheck = (sv: String) => "org.scalaz"               %% "scalaz-scalacheck-binding" % V.scalaz(sv)     % "test"

    val cats             = (sv: String) => "org.typelevel"            %% "cats"                      % V.cats(sv)
    val playJson         = (sv: String) => "com.typesafe.play"        %% "play-json"                 % V.play(sv)

    val scalaCheck       = (sv: String) => "org.scalacheck"           %% "scalacheck"                % V.scalaCheck(sv) % "test"
    val specs2           = (sv: String)  => "org.specs2"              %% "specs2-core"               % V.specs2(sv)     % "test"

    def at(sv: String)(libs: String => ModuleID*) =
      libs map { _(sv) }
  }

}
