organization := "me.lyh"
name := "shapeless-datatype"
description := "Shapeless utilities for common data types"

scalaVersion := "2.11.8"
crossScalaVersions := Seq("2.10.6", "2.11.8")
scalacOptions ++= Seq("-target:jvm-1.7", "-deprecation", "-feature", "-unchecked")
javacOptions ++= Seq("-source", "1.7", "-target", "1.7", "-Xlint:unchecked")

sbtavro.SbtAvro.avroSettings

libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.2",
  "com.google.apis" % "google-api-services-bigquery" % "v2-rev317-1.22.0",
  "com.google.cloud.datastore" % "datastore-v1-proto-client" % "1.2.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.2" % "test",
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.1" % "test",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.3" % "test"
)

libraryDependencies ++= (
  if (scalaBinaryVersion.value == "2.10")
    Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full))
  else
    Nil
)

// Release settings
releaseCrossBuild             := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
publishMavenStyle             := true
publishArtifact in Test       := false
sonatypeProfileName           := "com.spotify"
pomExtra                      := {
  <url>https://github.com/nevillelyh/shapeless-datatype</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    </license>
  </licenses>
  <scm>
    <url>git@github.com/spotify/ratatool.git</url>
    <connection>scm:git:git@github.com:spotify/ratatool.git</connection>
  </scm>
  <developers>
    <developer>
      <id>sinisa_lyh</id>
      <name>Neville Li</name>
      <url>https://twitter.com/sinisa_lyh</url>
    </developer>
  </developers>
}
