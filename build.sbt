name := "shapeless-datatype"
description := "Shapeless utilities for common data types"

val avroVersion = "1.12.0"
val bigqueryVersion = "v2-rev20250706-2.0.0"
val datastoreVersion = "2.31.0"
val jacksonVersion = "2.19.1"
val jodaTimeVersion = "2.14.0"
val magnolifyVersion = "0.8.0"
val protobufVersion = "3.25.8"
val scalacheckVersion = "1.18.1"
val shapelessVersion = "2.3.13"
val tensorflowVersion = "1.15.0"

val commonSettings = Seq(
  organization := "me.lyh",
  scalaVersion := "2.13.16",
  crossScalaVersions := Seq("2.12.20", "2.13.16"),
  scalacOptions ++= Seq("-target:jvm-1.8", "-deprecation", "-feature", "-unchecked"),
  // protobuf-lite is an older subset of protobuf-java and causes issues
  excludeDependencies += "com.google.protobuf" % "protobuf-lite",
  // Release settings
  publishTo := Some(
    if (isSnapshot.value) Opts.resolver.sonatypeOssSnapshots.head else Opts.resolver.sonatypeStaging
  ),
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  Test / publishArtifact := false,
  sonatypeProfileName := "me.lyh",
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url("https://github.com/nevillelyh/shapeless-datatype")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/nevillelyh/shapeless-datatype.git"),
      "scm:git:git@github.com:nevillelyh/shapeless-datatype.git"
    )
  ),
  developers := List(
    Developer(
      id = "sinisa_lyh",
      name = "Neville Li",
      email = "neville.lyh@gmail.com",
      url = url("https://twitter.com/sinisa_lyh")
    )
  )
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
  avro,
  bigquery,
  datastore,
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

lazy val avro: Project = Project(
  "avro",
  file("avro")
).settings(
  moduleName := "shapeless-datatype-avro",
  commonSettings,
  description := "Shapeless utilities for Apache Avro",
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "org.apache.avro" % "avro" % avroVersion % Provided
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
    "com.google.apis" % "google-api-services-bigquery" % bigqueryVersion % Provided,
    "joda-time" % "joda-time" % jodaTimeVersion % Provided,
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion % Test
  )
).dependsOn(
  core,
  test % "test->test"
)

lazy val datastore: Project = Project(
  "datastore",
  file("datastore")
).settings(
  commonSettings,
  moduleName := "shapeless-datatype-datastore",
  description := "Shapeless utilities for Google Cloud Datastore",
  libraryDependencies ++= Seq(
    "com.google.cloud.datastore" % "datastore-v1-proto-client" % datastoreVersion % Provided,
    "joda-time" % "joda-time" % jodaTimeVersion % Provided
  )
).dependsOn(
  core,
  test % "test->test"
)

lazy val tensorflow: Project = Project(
  "tensorflow",
  file("tensorflow")
).settings(
  moduleName := "shapeless-datatype-tensorflow",
  commonSettings,
  description := "Shapeless utilities for TensorFlow",
  libraryDependencies ++= Seq(
    "org.tensorflow" % "proto" % tensorflowVersion % Provided
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
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "com.spotify" %% "magnolify-scalacheck" % magnolifyVersion % Test,
    "org.scalacheck" %% "scalacheck" % scalacheckVersion % Test,
    "com.google.protobuf" % "protobuf-java" % protobufVersion % Test,
    "joda-time" % "joda-time" % jodaTimeVersion % Test
  )
)
