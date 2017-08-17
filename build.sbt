import Dependencies._
import Release._

lazy val tristate = (
  TristateProject("tristate", file("."))
    settings(releaseSettings: _*) 
    settings(
      packagedArtifacts := Map.empty // don't publish the default aggregate root project
    )
    aggregate(core, play, scalaz)
    dependsOn(core, play, scalaz)
)

lazy val core = (
  TristateProject("tristate-core")
    settings(
      name                 :=  "tristate-core",
      libraryDependencies ++= Libs.at(scalaVersion.value)()
    )
)

lazy val play = (
  TristateProject("tristate-play")
    settings(
      name                 := "tristate-play",
      crossScalaVersions   := Seq(V.scala_2_11),
      libraryDependencies ++= Libs.at(scalaVersion.value)(
        Libs.playJson,
        Libs.specs2,
      )
    )
    dependsOn(core % "compile->compile;test->test")
)

lazy val scalaz = (
  TristateProject("tristate-scalaz")
    settings(
      name                :=  "tristate-scalaz",
      libraryDependencies ++= Libs.at(scalaVersion.value)(
        Libs.scalaz,
        Libs.scalazScalaCheck
      )
    )
    dependsOn(core % "compile->compile;test->test")
)
