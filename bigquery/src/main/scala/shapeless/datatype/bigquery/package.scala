package shapeless.datatype

import shapeless._
import shapeless.datatype.mappable.{CanNest, FromMappable, ToMappable}

package object bigquery extends BigQueryMappableType {
  type BigQueryMap = java.util.Map[String, Any]
  type FromTableRow[L <: HList] = FromMappable[L, BigQueryMap]
  type ToTableRow[L <: HList] = ToMappable[L, BigQueryMap]

  implicit object BigQueryCanNest extends CanNest[BigQueryMap]
}
