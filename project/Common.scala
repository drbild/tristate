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
    scalaVersion := V.scala,

    scalacOptions ++= Seq("-target:jvm-1.7"),
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
      "-Xfuture",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-unused-import"),

    javacOptions in (Compile) ++= Seq("-source", "1.8"),
    javacOptions in (Compile, compile) ++= Seq("-target", "1.7"),

    updateOptions := updateOptions.value.withCachedResolution(true),
    resolvers     ++= Dependencies.resolvers,

    libraryDependencies ++= Seq(Libs.scalaCheck),

    // Release options
    organization    := "org.davidbild",
    pomExtra        := pomExtraVal,
    pomPostProcess  := pomPostProcessVal,
    credentials    ++= credentialsVal,

    // keep headers updated
    organizationName := "David R. Bild",
    startYear        := Some(2016),
    licenses         += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),

    // sbt console prompt
    shellPrompt     := projectPrompt
  )

  val pomExtraVal: xml.NodeBuffer = (
    <url>https://github.com/drbild/tristate</url>
      <licenses>
        <license>
          <name>Apache</name>
          <url>http://www.opensource.org/licenses/Apache-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
    <scm>
      <url>git@github.com:drbild/tristate.git</url>
      <connection>scm:git:git@github.com:drbild/tristate.git</connection>
    </scm>
    <developers>
      <developer>
        <id>drbild</id>
        <name>David R. Bild</name>
        <url>https://github.com/drbild</url>
      </developer>
    </developers>
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
