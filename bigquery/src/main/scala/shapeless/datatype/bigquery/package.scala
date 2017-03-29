package shapeless.datatype

import shapeless._
import shapeless.datatype.mappable.{FromMappable, ToMappable}

package object bigquery extends BigQueryMappableType {
  type TableRow = java.util.Map[String, AnyRef]
  type FromTableRow[L] = FromMappable[L, TableRow]
  type ToTableRow[L] = ToMappable[L, TableRow]
}
