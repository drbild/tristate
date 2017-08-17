import sbt._
import Keys._

object Dependencies {
  val resolvers = Seq(
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
  )

  // Versions
  object V {
    val scala       = "2.11.11"
    val scalaz      = "7.1.5"
    val cats        = "0.4.1"
    val play        = "2.4.6"

    val scalaCheck  = "1.11.4"
    val specs2      = "3.7.2"
  }

  // Libraries
  object Libs {
    val scalaz              = "org.scalaz"               %% "scalaz-core"               % V.scalaz
    val scalazScalaCheck    = "org.scalaz"               %% "scalaz-scalacheck-binding" % V.scalaz  % "test"

    val cats                = "org.typelevel"            %% "cats"                      % V.cats
    val playJson            = "com.typesafe.play"        %% "play-json"                 % V.play

    val scalaCheck          = "org.scalacheck"           %% "scalacheck"                % V.scalaCheck % "test"
    val specs2              = "org.specs2"               %% "specs2-core"               % V.specs2     % "test"
  }
}
