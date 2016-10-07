organization := "me.lyh"
name := "shapeless-datatype"
description := "Shapeless utilities for common data types"
version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"
crossScalaVersions := Seq("2.10.6", "2.11.8")
scalacOptions ++= Seq("-target:jvm-1.7", "-deprecation", "-feature", "-unchecked")
javacOptions ++= Seq("-source", "1.7", "-target", "1.7", "-Xlint:unchecked")

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
