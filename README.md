shapeless-datatype
==================

[![Build Status](https://travis-ci.org/nevillelyh/shapeless-datatype.svg?branch=master)](https://travis-ci.org/nevillelyh/shapeless-datatype)
[![codecov.io](https://codecov.io/github/nevillelyh/shapeless-datatype/coverage.svg?branch=master)](https://codecov.io/github/nevillelyh/shapeless-datatype?branch=master)
[![GitHub license](https://img.shields.io/github/license/nevillelyh/shapeless-datatype.svg)](./LICENSE)

[Shapeless](https://github.com/milessabin/shapeless) utilities for common data types

# Modules

This library includes the following modules.

- `shapeless-datatype-core`
- `shapeless-datatype-bigquery`
- `shapeless-datatype-datastore`

Due to library dependency `shapeless-datatype-datastore` is built for different versions of Datastore client, e.g. `shapeless-datatype-datastore_1.3` for `datastore-v1-proto-client` 1.3.0.

# Core

Core includes the following components.

- A `MappableType` for generic conversion between case class and other data types, used by BigQuery and Datastore modules.
- A `RecordMapperType` for generic conversion between case class types.
- A `RecordMatcherType` for generic type-based equality check bewteen case classes.
- A `LensMatcher` for generic lens-based equality check between case classes.

## RecordMapperType

`RecordMapperType[A, B]` maps instances of case class `A` and `B` with different field types.

```scala
import shapeless._
import shapeless.datatype.record._
import scala.language.implicitConversions

// records with same field names but different types
case class Point1(x: Double, y: Double, label: String)
case class Point2(x: Float, y: Float, label: String)

// implicit conversion bewteen fields of different types
implicit def f2d(x: Float) = x.toDouble
implicit def d2f(x: Double) = x.toFloat

val m = RecordMapperType[Point1, Point2]
m.to(Point1(0.5, -0.5, "a"))  // Point2(0.5,-0.5,a)
m.from(Point2(0.5, -0.5, "a")) // Point1(0.5,-0.5,a)
```

## RecordMatcherType

`RecordMatcherType[T]` performs equality check of instances of case class `T` with custom logic based on field types.

```scala
import shapeless._
import shapeless.datatype.record._

case class Record(id: String, name: String, value: Int)

// custom comparator for String type
implicit def compareStrings(x: String, y: String) = x.toLowerCase == y.toLowerCase

val m = RecordMatcherType[Record]
Record("a", "RecordA", 10) == Record("A", "RECORDA", 10)  // false

// compareStrings is applied to all String fields
m(Record("a", "RecordA", 10) == Record("A", "RECORDA", 10))  // true
```

## LensMatcher

`LensMatcher[T]` performs equality check of instances of case class `T` with custom logic based on Lenses.

```scala
import shapeless._
import shapeless.datatype.record._

case class Record(id: String, name: String, value: Int)

// compare String fields id and name with different logic
val m = LensMatcher[Record]
  .on(_ >> 'id)(_.toLowerCase == _.toLowerCase)
  .on(_ >> 'name)(_.length == _.length)

Record("a", "foo", 10) == Record("A", "bar", 10)  // false
m(Record("a", "foo", 10), Record("A", "bar", 10))  // true
```

# BigQueryType

`BigQueryType[T]` maps bewteen case class `T` and BigQuery `TableRow`.

```scala
import shapeless._
import shapeless.datatype.bigquery._

case class City(name: String, code: String, lat: Double, long: Double)

val t = BigQueryType[City]
val r = t.toTableRow(City("New York", "NYC", 40.730610, -73.935242))
val c = t.fromTableRow(r)
```

# DatastoreType

`DatastoreType[T]` maps between case class `T` and Datastore `Entity` or `Entity.Builder`.

```scala
import shapeless._
import shapeless.datatype.datastore._

case class City(name: String, code: String, lat: Double, long: Double)

val t = DatastoreType[City]
val r = t.toEntity(City("New York", "NYC", 40.730610, -73.935242))
val c = t.fromEntity(r)
```

# License

Copyright 2016 Neville Li.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
