import sbt._
import sbt.Keys._

object Build extends Build {
  
  val dependencies = Seq(
    "net.contentobjects.jnotify" % "jnotify" % "0.94" //watches changes of files, uses native libraries
    , "com.typesafe.akka" % "akka-actor_2.10" % "2.1.2" //event-driven programming with messages and actors
    , "joda-time" % "joda-time" % "2.2" //library for timing
    , "com.google.guava" % "guava" % "14.0.1" //functional programming and utils for collections
    , "org.apache.commons" % "commons-lang3" % "3.1" //functions missing in the Java library
    , "commons-io" % "commons-io" % "2.4" //file and stream operations
    , "org.slf4j" % "slf4j-api" % "1.7.5" //logging. logger for classes
    , "ch.qos.logback" % "logback-classic" % "1.0.3" //configuration of logging
    , "ch.qos.logback" % "logback-core" % "1.0.3" //configuration of logging
    , "com.novocode" % "junit-interface" % "0.8" % "test->default" //needed by JUnit with SBT
    , "junit" % "junit" % "4.11" % "test" //test suite
    , "org.easytesting" % "fest-assert" % "1.4" % "test"//assertions with better messages
  )

  lazy val main = Project(
    id = "sync-demon",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "sync-demon"
      , organization := "org.docear"
      , version := "0.1-SNAPSHOT"
      , scalaVersion := "2.10.0"
      , resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
      , resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
      , libraryDependencies ++= dependencies
      , javacOptions ++= Seq("-target", "1.6") ++ Seq("-source", "1.6")
    ) ++ seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)
  )
}
