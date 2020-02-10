import sbt._
import Keys._

import Dependencies.{Libs, V}

object Common {

  val projectPrompt = { state: State =>
    val extracted = Project.extract(state)
    import extracted._
    (name in currentRef get structure.data).map { name =>
      "[" + name + "] $ "
    }.getOrElse("> ")
  }

  val commonSettings: Seq[Setting[_]] = Seq(
    scalaVersion := "2.11.12",
    crossScalaVersions := Seq("2.11.12", "2.12.3", "2.13.1"),

    scalacOptions ++=  Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xlint",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen"),
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 11)) => Seq("-target:jvm-1.7", "-Ywarn-unused-import", "-Xfuture")
      case Some((2, 12)) => Seq("-Ywarn-unused-import", "-Xfuture")
      case _             => Seq("-Wunused:imports")
    }),
    scalacOptions in (Compile, doc) := (scalacOptions in (Compile, doc)).value.filter(_ != "-Xfatal-warnings"),

    updateOptions := updateOptions.value.withCachedResolution(true),
    resolvers     ++= Dependencies.resolvers,

    libraryDependencies ++= Libs.at(scalaVersion.value)(
      Libs.scalaCheck
    ),

    autoAPIMappings := true,

    // Release options
    publishTo       := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging),
    organization    := "org.davidbild",
    pomPostProcess  := pomPostProcessVal,
    credentials    ++= credentialsVal,

    licenses         += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage         := Some(url("https://github.com/drbild/tristate")),
    scmInfo          := Some(ScmInfo(url("https://github.com/drbild/tristate.git"), "scm:git:git@github.com:drbild/tristate.git")),
    developers       := List(Developer(id="drbild", name="David R. Bild", email="david@davidbild.org", url=url("https://github.com/drbild"))),

    // keep headers updated
    organizationName := "David R. Bild",
    startYear        := Some(2016),

    // sbt console prompt
    shellPrompt     := projectPrompt
  )

  /* strip test deps from pom */
  import scala.xml._
  import scala.xml.transform._
  lazy val pomPostProcessVal = { node: Node =>
    def stripIf(f: Node => Boolean) = new RewriteRule {
      override def transform(n: Node) = if (f(n)) NodeSeq.Empty else n
    }
    val stripTestScope = stripIf(n => n.label == "dependency" && (n \ "scope").text == "test")
    new RuleTransformer(stripTestScope).transform(node)(0)
  }

  val credentialsVal: Seq[Credentials] = {
    val realm    = "Sonatype Nexus Repository Manager"
    val host     = "oss.sonatype.org"
    val cred = for {
      username <- scala.util.Try(sys.env("NEXUS_USERNAME")).toOption
      password <- scala.util.Try(sys.env("NEXUS_PASSWORD")).toOption
    } yield Credentials(realm, host, username, password)
    cred.toList
  }

}

object TristateProject {
  import Common._

  def apply(name: String): Project = TristateProject(name, file(name))
  def apply(name: String, file: File): Project =  Project(name, file).settings(commonSettings:_*)
}
