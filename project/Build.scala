import sbt._
import Keys._
import com.github.siasia.WebPlugin

object BuildSettings {
    val buildOrganization = "Havoc Pennington"
    val buildVersion = "0.1"
    val buildScalaVersion = "2.9.0-1"

    val globalSettings = Seq(
        organization := buildOrganization,
        version := buildVersion,
        //retrieveManaged := true, // for debugging
        scalaVersion := buildScalaVersion,
        shellPrompt := ShellPrompt.buildShellPrompt,
        resolvers := Seq(Resolvers.akkaRepo, Resolvers.scalaToolsSnapshotsRepo,
                Resolvers.glassfishRepo))

    val projectSettings = Defaults.defaultSettings ++ globalSettings
}

object ShellPrompt {
    object devnull extends ProcessLogger {
        def info(s : => String) {}
        def error(s : => String) {}
        def buffer[T](f : => T) : T = f
    }

    val current = """\*\s+([\w-\.]+)""".r

    def gitBranches = ("git branch --no-color" lines_! devnull mkString)

    val buildShellPrompt = { (state : State) =>
        {
            val currBranch =
                current findFirstMatchIn gitBranches map (_ group (1)) getOrElse "-"
            val currProject = Project.extract(state).currentProject.id
            "%s:%s:%s> ".format(currProject, currBranch, BuildSettings.buildVersion)
        }
    }
}

object Resolvers {
    val akkaRepo = "Akka Repo" at "http://akka.io/repository"
    val scalaToolsSnapshotsRepo = "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"
    val glassfishRepo = "Glassfish Repo" at "http://download.java.net/maven/glassfish"
}

object Dependencies {
    final val AKKA_VERSION = "1.1.2"
    val akkaActor = "se.scalablesolutions.akka" % "akka-actor" % AKKA_VERSION
    val akkaHttp = "se.scalablesolutions.akka" % "akka-http" % AKKA_VERSION

    final val JETTY_VERSION = "7.4.0.v20110414"

    val javaxServlet = "org.glassfish" % "javax.servlet" % "3.0" % "provided"
    val jettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % JETTY_VERSION % "jetty"
}


object AkkaHttpBuild extends Build {
    import BuildSettings._
    import Dependencies._
    import Resolvers._

    override lazy val settings = super.settings ++ globalSettings

    lazy val root = Project("akka-http-test",
        file("."),
        settings = projectSettings) aggregate(hello)

    // I can't figure out what this does
    /*
    val excludeAkkaJetty = <dependencies>
                              <dependency org="se.scalablesolutions.akka" name="akka-http" rev={ AKKA_VERSION }>
                                  <exclude module="jetty"/>
                              </dependency>
                          </dependencies>
                          */

    lazy val hello =  Project("hello",
        file("hello"),
        settings = projectSettings ++ WebPlugin.webSettings ++
        Seq(WebPlugin.jettyConfFiles := WebPlugin.JettyConfFiles(None, Some(file(".") / "hello" / "conf" / "jetty-env.xml")),
                libraryDependencies := Seq(akkaActor, akkaHttp, javaxServlet, jettyWebapp)
            /* , ivyXML := excludeAkkaJetty) */))
}
