import Dependencies.*
import Release.*

lazy val tristate = (
  TristateProject("tristate", file("."))
    settings(releaseSettings *)
    settings(
      packagedArtifacts := Map.empty // don't publish the default aggregate root project
    )
    aggregate(core, play, cats, scalaz)
    dependsOn(core, play, cats, scalaz)
)

lazy val core = (
  TristateProject("tristate-core")
    settings(
      name                 :=  "tristate-core",
      libraryDependencies ++= Seq()
    )
)

lazy val play = (
  TristateProject("tristate-play")
    settings(
      name                 := "tristate-play",
      libraryDependencies ++= Seq(
        Libs.playJson,
        Libs.specs2
      )
    )
    dependsOn(core % "compile->compile;test->test")
)

lazy val cats = (
  TristateProject("tristate-cats")
    settings(
      name                :=  "tristate-cats",
      libraryDependencies ++= Seq(
        Libs.cats,
        Libs.catsLaws,
        Libs.scalaTest,
        Libs.disciplineScalaTest
      )
    )
    dependsOn(core % "compile->compile;test->test")
)

lazy val scalaz = (
  TristateProject("tristate-scalaz")
    settings(
      name                :=  "tristate-scalaz",
      libraryDependencies ++= Seq(
        Libs.scalaz,
        Libs.scalazScalaCheck
      )
    )
    dependsOn(core % "compile->compile;test->test")
)
