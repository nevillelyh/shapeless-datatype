package me.lyh.shapeless.datatype

import me.lyh.shapeless.datatype.mappable.{FromMappable, ToMappable}
import shapeless._

package object bigquery extends BigQueryMappableType {
  type TableRow = java.util.Map[String, AnyRef]
  type FromTableRow[L <: HList] = FromMappable[L, TableRow]
  type ToTableRow[L <: HList] = ToMappable[L, TableRow]
}
