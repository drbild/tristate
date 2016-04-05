import sbt._
import Keys._

import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleaseStateTransformations._
import com.typesafe.sbt.pgp.PgpKeys
import UpdateReadmePlugin.autoImport._

object Release {
  val releaseSettings: Seq[Def.Setting[_]] = Seq(
    // Adds updateReadme, commitReadme, and sonatypeReleaseAll to defaults                                           
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      runUpdateReadme,
      commitReleaseVersion,
      releaseStepTask(commitReadme),
      tagRelease,
      runPublishArtifacts,
      setNextVersion,
      commitNextVersion,
      releaseStepCommand("sonatypeReleaseAll"),
      pushChanges),

    releaseCrossBuild := true,
    publishMavenStyle := true,

    releaseTagName       := s"releases/${(version in ThisBuild).value}",
    releaseTagComment    := s"release: ${(version in ThisBuild).value}",
    releaseCommitMessage := s"version: bump to ${(version in ThisBuild).value}"
  )

  lazy val runUpdateReadme: ReleaseStep = ReleaseStep(
    action = { st: State =>
      val extracted = Project.extract(st)
      val ref = extracted.get(thisProjectRef)
      extracted.runAggregated(updateReadme in Global in ref, st)
    }
  )

  lazy val runPublishArtifacts: ReleaseStep = ReleaseStep(
    action = { st: State =>
      val extracted = Project.extract(st)
      val ref = extracted.get(thisProjectRef)
      extracted.runAggregated(PgpKeys.publishSigned in Global in ref, st)
    },
    enableCrossBuild = true
  )
}
