package shapeless.datatype

import org.apache.avro.generic.GenericRecord
import shapeless._
import shapeless.datatype.mappable.{CanNest, FromMappable, ToMappable}

package object avro extends AvroMappableType {
  type AvroRecord = Either[AvroBuilder, GenericRecord]
  type FromAvroRecord[L <: HList] = FromMappable[L, AvroRecord]
  type ToAvroRecord[L <: HList] = ToMappable[L, AvroRecord]

  implicit object AvroCanNest extends CanNest[AvroRecord]
}
