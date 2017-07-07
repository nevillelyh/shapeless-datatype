package shapeless.datatype

import shapeless._
import shapeless.datatype.mappable.{CanNest, FromMappable, ToMappable}

package object bigquery extends BigQueryMappableType {
  type TableRow = java.util.Map[String, Any]
  type FromTableRow[L <: HList] = FromMappable[L, TableRow]
  type ToTableRow[L <: HList] = ToMappable[L, TableRow]

  implicit object BigQueryCanNest extends CanNest[TableRow]
}
