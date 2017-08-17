import sbt._
import Keys._

object Dependencies {
  val resolvers = Seq(
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
  )

  // Versions
  object V {
    val scala_2_11  = "2.11.11"
    val scala_2_12  = "2.12.3"
    val scala       = scala_2_11

    val scalaz      = "7.2.15"
    val cats        = "1.0.0-MF"
    val play        = "2.4.6"

    val scalaCheck  = "1.12.6"
    val specs2      = "3.9.4"
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
