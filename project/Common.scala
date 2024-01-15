import Dependencies.Libs
import sbt.*
import sbt.Keys.*

object Common {



  val commonSettings: Seq[Setting[?]] = Seq(
    scalaVersion := "3.3.1",

    scalacOptions ++=  Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-unchecked",
      "-Xfatal-warnings"),
    Compile / doc / scalacOptions := (Compile / doc / scalacOptions).value.filter(_ != "-Xfatal-warnings"),

    updateOptions := updateOptions.value.withCachedResolution(true),
    resolvers     ++= Dependencies.resolvers,

    libraryDependencies ++= Seq(Libs.scalaCheck)
    ,

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

  )

  /* strip test deps from pom */
  import scala.xml.*
  import scala.xml.transform.*
  lazy val pomPostProcessVal: Node => Node = { node: Node =>
    def stripIf(f: Node => Boolean) = new RewriteRule {
      override def transform(n: Node): NodeSeq = if (f(n)) NodeSeq.Empty else n
    }
    val stripTestScope: RewriteRule = stripIf(n => n.label == "dependency" && (n \ "scope").text == "test")
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
  import Common.*

  def apply(name: String): Project = TristateProject(name, file(name))
  def apply(name: String, file: File): Project =  Project(name, file).settings(commonSettings *)
}
