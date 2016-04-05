import sbt._
import sbt.Keys._

import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.Vcs

object UpdateReadmePlugin extends AutoPlugin {

  object autoImport {
    val readmeFile          = settingKey[File]("The readme file.")
    val readmeCommitMessage = taskKey[String]("The commit message to use when updating.")
    val updateReadme        = taskKey[Unit]("The task to update the readme file.")
    val commitReadme        = taskKey[Unit]("The task to commit the readme file to VCS.")
  }

  import autoImport._

  private def commitReadmeDefault(file: File, optVcs: Option[Vcs], log: Logger): Unit = {
  }

  override def trigger = allRequirements

  override lazy val projectSettings = Seq(
    readmeFile          := file("README.md"),
    readmeCommitMessage := s"readme: bump deps to ${(version in ThisBuild).value}",


    updateReadme := readmeFile.synchronized { // Ensure subprojects update the readme file atomically
      val log  = streams.value.log

      val file = readmeFile.value
      val o    = organization.value
      val n    = name.value
      val v    = version.value

      val pattern = (s"""(\\s*)libraryDependencies \\+= "${o}" %% "${n}" % "(.+)"(.*)""").r
      val readme  = IO.readLines(file).map{ _ match {
        case pattern(left, _, right) =>
          s"""${left}libraryDependencies += "${o}" %% "${n}" % "${v}"${right}"""
        case line @ _ =>
          line
      }}
      IO.writeLines(file, readme)
    },

    commitReadme := readmeFile.synchronized { // Ensure subprojects commit changes atomically
      val log  = streams.value.log
      val vcs  = releaseVcs.value.getOrElse(sys.error("Aborting. CWD is not a supported repository."))

      val file = readmeFile.value
      val base = vcs.baseDir

      val relative = IO.relativize(base, file).getOrElse(sys.error(s"[${file}] is outside of the repository."))
      val message  = readmeCommitMessage.value

      vcs.add(relative) !! log
      val status = (vcs.status.!!).trim
      if (status.nonEmpty) {
        vcs.commit(message) ! log
      }
    }
  )
}
