name := "shapeless-datatype"
description := "Shapeless utilities for common data types"

val algebirdVersion = "0.13.0"
val avroVersion = "1.8.2"
val bigqueryVersion = "v2-rev350-1.22.0"
val jacksonVersion = "2.8.8.1"
val jodaTimeVersion = "2.9.9"
val paradiseVersion = "2.1.0"
val protobufVersion = "3.3.1"
val scalacheckShapelessVersion = "1.1.5"
val scalacheckVersion = "1.13.5"
val shapelessVersion = "2.3.2"
val tensorflowVersion = "1.2.0"

val commonSettings = Seq(
  organization := "me.lyh",

  scalaVersion := "2.12.2",
  crossScalaVersions := Seq("2.11.11", "2.12.2"),
  scalacOptions ++= Seq("-target:jvm-1.8", "-deprecation", "-feature", "-unchecked"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint:unchecked"),

  libraryDependencies ++= (
    if (scalaBinaryVersion.value == "2.10")
      Seq(compilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full))
    else
      Nil
  ),
  // protobuf-lite is an older subset of protobuf-java and causes issues
  excludeDependencies += "com.google.protobuf" % "protobuf-lite",

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
  "root",
  file(".")
).settings(
  commonSettings ++ noPublishSettings
).aggregate(
  core,
  algebird,
  avro,
  bigquery,
  datastore11,
  datastore12,
  datastore13,
  tensorflow,
  test
)

lazy val core: Project = Project(
  "core",
  file("core")
).settings(
  moduleName := "shapeless-datatype-core",
  commonSettings,
  description := "Shapeless utilities for common data types",
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % shapelessVersion
  )
).dependsOn(
  test % "test->test"
)

lazy val algebird: Project = Project(
  "algebird",
  file("algebird")
).settings(
  moduleName := "shapeless-datatype-algebird",
  commonSettings,
  description := "Shapeless utilities for Twitter Algebird",
  libraryDependencies ++= Seq(
    "com.chuusai" %% "shapeless" % shapelessVersion,
    "com.twitter" %% "algebird-core" % algebirdVersion
  )
).dependsOn(
  core % "test",
  test % "test->test"
)

lazy val avro: Project = Project(
  "avro",
  file("avro")
).settings(
  moduleName := "shapeless-datatype-avro",
  commonSettings,
  description := "Shapeless utilities for Apache Avro",
  libraryDependencies ++= Seq(
    "org.apache.avro" % "avro" % avroVersion,
    "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )
).dependsOn(
  core,
  test % "test->test"
)

lazy val bigquery: Project = Project(
  "bigquery",
  file("bigquery")
).settings(
  moduleName := "shapeless-datatype-bigquery",
  commonSettings,
  description := "Shapeless utilities for Google Cloud BigQuery",
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "com.google.apis" % "google-api-services-bigquery" % bigqueryVersion,
    "joda-time" % "joda-time" % jodaTimeVersion % "provided",
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion % "test"
  )
).dependsOn(
  core,
  test % "test->test"
)

def datastoreProject(binaryVersion: String, version: String): Project = Project(
  "datastore_" + binaryVersion.replace(".", ""),
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
  test % "test->test"
)
lazy val datastore11 = datastoreProject("1.1", "1.1.0")
lazy val datastore12 = datastoreProject("1.2", "1.2.0")
lazy val datastore13 = datastoreProject("1.3", "1.3.0")

lazy val tensorflow: Project = Project(
  "tensorflow",
  file("tensorflow")
).settings(
  moduleName := "shapeless-datatype-tensorflow",
  commonSettings,
  description := "Shapeless utilities for TensorFlow",
  libraryDependencies ++= Seq(
    "org.tensorflow" % "proto" % tensorflowVersion
  )
).dependsOn(
  core,
  test % "test->test"
)

lazy val test: Project = Project(
  "test",
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
