import sbt._
import sbt.Keys._
import de.johoop.jacoco4sbt._
import JacocoPlugin._
import sbt.Tests.Setup

object Build extends Build {
  
  val dependencies = Seq(
    "net.contentobjects.jnotify" % "jnotify" % "0.94" //watches changes of files, uses native libraries
    , "com.typesafe.akka" % "akka-actor_2.10" % "2.1.2" //event-driven programming with messages and actors
    , "joda-time" % "joda-time" % "2.2" //library for timing
    , "com.google.guava" % "guava" % "14.0.1" //functional programming and utils for collections
    , "org.apache.commons" % "commons-lang3" % "3.1" //functions missing in the Java library
    , "commons-io" % "commons-io" % "2.4" //file and stream operations
    , "org.slf4j" % "slf4j-api" % "1.7.5" //logging. logger for classes
    , "ch.qos.logback" % "logback-classic" % "1.0.12" //configuration of logging
    , "ch.qos.logback" % "logback-core" % "1.0.12" //configuration of logging
    , "uk.org.lidalia" % "sysout-over-slf4j" % "1.0.2" //log System.{out,err}.println to logback
    , "com.novocode" % "junit-interface" % "0.8" % "test->default" //needed by JUnit with SBT
    , "junit" % "junit" % "4.11" % "test" //test suite
    , "org.easytesting" % "fest-assert" % "1.4" % "test"//assertions with better messages
  )

  lazy val projectReleaseSettings = {
    lazy val releaseFolder = Option(System.getProperty("release.folder")).map(x => Resolver.file("file", new File(x)))
    Seq(
      publishMavenStyle := true
      , sources in (Compile, doc) ~= (_ filter (f => false)) //sbt publish does not work with Java settings, i.e.: javadoc: error - invalid flag: -target
      , publishArtifact in Test := false
      , publishTo <<= version { (v: String) =>
        if (v.trim.endsWith("SNAPSHOT"))
          None //Please publish SNAPSHOT releases only to a local repository with `sbt publish-local`.
        else
          releaseFolder //Please provide a release directory with `sbt -Drelease.folder=<path> publish`.
      }
      , pomIncludeRepository := { _ => false }
    )
  }

  lazy val main = Project(
    id = "sync-daemon",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "sync-daemon"
      , organization := "org.docear"
      , version := "0.1-SNAPSHOT"
      , crossPaths := false
      , scalaVersion := "2.10.0"
      , resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
      , resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
      , libraryDependencies ++= dependencies
      , javacOptions ++= Seq("-target", "1.6") ++ Seq("-source", "1.6")
      , javacOptions ++= Seq("-Xlint:-options")
      , javacOptions ++= Seq("-Xlint:deprecation")
      , initialize in Runtime ~= { _ =>
        System.setProperty("started_with_sbt", "true")//used to wire in correct config file
      }
      , testOptions += Setup( cl =>
        /*
        used to fix the following problem:
        SLF4J: The following loggers will not work because they were created
        SLF4J: during the default configuration phase of the underlying logging system.
        SLF4J: See also http://www.slf4j.org/codes.html#substituteLogger

        see http://stackoverflow.com/questions/7898273/how-to-get-logging-working-in-scala-unit-tests-with-testng-slf4s-and-logback
         */
        cl.loadClass("org.slf4j.LoggerFactory").
          getMethod("getLogger",cl.loadClass("java.lang.String")).
          invoke(null,"ROOT")
      )
    ) ++
      seq(com.github.retronym.SbtOneJar.oneJarSettings: _*) ++
      seq(jacoco.settings : _*) ++
      seq(parallelExecution in jacoco.Config := false) ++
      projectReleaseSettings
  )
}
