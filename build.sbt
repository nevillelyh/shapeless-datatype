name := "shapeless-datatype"
description := "Shapeless utilities for common data types"

val bigqueryVersion = "v2-rev317-1.22.0"
val jacksonVersion = "2.8.4"
val jodaTimeVersion = "2.9.4"
val paradiseVersion = "2.1.0"
val protobufVersion = "3.1.0"
val scalacheckShapelessVersion = "1.1.1"
val scalacheckVersion = "1.13.2"
val shapelessVersion = "2.3.2"

val commonSettings = Seq(
  organization := "me.lyh",

  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8"),
  scalacOptions ++= Seq("-target:jvm-1.7", "-deprecation", "-feature", "-unchecked"),
  javacOptions ++= Seq("-source", "1.7", "-target", "1.7", "-Xlint:unchecked"),

  libraryDependencies ++= (
    if (scalaBinaryVersion.value == "2.10")
      Seq(compilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full))
    else
      Nil
  ),

  // Release settings
  releaseCrossBuild             := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle             := true,
  publishArtifact in Test       := false,
  sonatypeProfileName           := "me.lyh",
  pomExtra                      := {
    <url>https://github.com/nevillelyh/shapeless-datatype</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <url>git@github.com/nevillelyh/shapeless-datatype.git</url>
      <connection>scm:git:git@github.com:nevillelyh/shapeless-datatype.git</connection>
    </scm>
    <developers>
      <developer>
        <id>sinisa_lyh</id>
        <name>Neville Li</name>
        <url>https://twitter.com/sinisa_lyh</url>
      </developer>
    </developers>
  }
)

val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val root: Project = Project(
  "shapeless-datatype",
  file(".")
).settings(
  commonSettings ++ noPublishSettings
).aggregate(
  core,
  bigquery,
  datastore11,
  datastore12,
  datastore13
)

lazy val core: Project = Project(
  "shapeless-datatype-core",
  file("core")
).settings(
  commonSettings,
  description := "Shapeless utilities for common data types",
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % shapelessVersion
  )
).dependsOn(test % "test")

lazy val bigquery: Project = Project(
  "shapeless-datatype-bigquery",
  file("bigquery")
).settings(
  commonSettings,
  description := "Shapeless utilities for Google Cloud BigQuery",
  libraryDependencies ++= Seq(
    "com.google.apis" % "google-api-services-bigquery" % bigqueryVersion,
    "com.google.protobuf" % "protobuf-java" % protobufVersion % "provided",
    "joda-time" % "joda-time" % jodaTimeVersion % "provided",
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion % "test"
  )
).dependsOn(
  core,
  test % "test"
)

def datastoreProject(binaryVersion: String, version: String): Project = Project(
  "shapeless-datatype-datastore_" + binaryVersion.replace(".", ""),
  file("datastore_" + binaryVersion)
).settings(
  commonSettings,
  moduleName := "shapeless-datatype-datastore_" + binaryVersion,
  description := "Shapeless utilities for Google Cloud Datastore",
  unmanagedSourceDirectories in Compile += (baseDirectory in ThisBuild).value / "datastore/src/main/scala",
  scalaSource in Test := (baseDirectory in ThisBuild).value / "datastore/src/test/scala",
  libraryDependencies ++= Seq(
    "com.google.cloud.datastore" % "datastore-v1-proto-client" % version,
    "joda-time" % "joda-time" % jodaTimeVersion % "provided"
  )
).dependsOn(
  core,
  test % "test"
)
lazy val datastore11 = datastoreProject("1.1", "1.1.0")
lazy val datastore12 = datastoreProject("1.2", "1.2.0")
lazy val datastore13 = datastoreProject("1.3", "1.3.0")

lazy val test: Project = Project(
  "shapeless-datatype-test",
  file("test")
).settings(
  commonSettings ++ noPublishSettings,
  description := "Shapeless utilities for common data types - shared code for unit test",
  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % scalacheckVersion,
    "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % scalacheckShapelessVersion,
    "com.google.protobuf" % "protobuf-java" % protobufVersion,
    "joda-time" % "joda-time" % jodaTimeVersion
  )
)
