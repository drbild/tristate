import sbt._
import Keys._

object Dependencies {
  val resolvers = Seq(
    "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
  )

  // Versions
  object V {
    val scalaz = "7.3.8"
    val cats = "2.10.0"
    val play = "3.0.1"

    val scalaCheck = "1.17.0"
    val scalaTest = "3.2.17"
    val specs2 = "5.4.2"
    val scalazScalaCheck = "7.3.8"

    val disciplineScalaTest = "2.2.0"

  }

  // Libraries
  object Libs {
    val scalaz = "org.scalaz" %% "scalaz-core" % V.scalaz
    val scalazScalaCheck = "org.scalaz" %% "scalaz-scalacheck-binding" % V.scalazScalaCheck % "test"

    val cats = "org.typelevel" %% "cats-core" % V.cats
    val catsLaws = "org.typelevel" %% "cats-laws" % V.cats % "test"
    val disciplineScalaTest = "org.typelevel" %% "discipline-scalatest" % V.disciplineScalaTest % "test"

    val playJson = "org.playframework" %% "play-json" % V.play

    val scalaCheck = "org.scalacheck" %% "scalacheck" % V.scalaCheck % "test"
    val scalaTest = "org.scalatest" %% "scalatest" % V.scalaTest % "test"
    val specs2 = "org.specs2" %% "specs2-core" % V.specs2 % "test"


  }

}
