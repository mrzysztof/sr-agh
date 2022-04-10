ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "rabbitmq"
  )

libraryDependencies += "com.newmotion" %% "akka-rabbitmq" % "6.0.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.11" % Runtime