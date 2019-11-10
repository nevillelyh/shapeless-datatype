package shapeless.datatype.avro

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import shapeless._

import scala.reflect.runtime.universe._

class AvroType[A] extends Serializable {
  def fromGenericRecord[L <: HList](
    m: GenericRecord
  )(implicit gen: LabelledGeneric.Aux[A, L], fromL: FromAvroRecord[L]): Option[A] =
    fromL(Right(m)).map(gen.from)
  def toGenericRecord[L <: HList](
    a: A
  )(implicit gen: LabelledGeneric.Aux[A, L], toL: ToAvroRecord[L], tt: TypeTag[A]): GenericRecord =
    toL(gen.to(a)).left.get.build(AvroSchema[A])
}

object AvroType {
  def apply[A: TypeTag]: AvroType[A] = new AvroType[A]

  def at[V: TypeTag](
    schemaType: Schema.Type
  )(fromFn: Any => V, toFn: V => Any): BaseAvroMappableType[V] = {
    AvroSchema.register(implicitly[TypeTag[V]].tpe, schemaType)
    new BaseAvroMappableType[V] {
      override def from(value: Any): V = fromFn(value)
      override def to(value: V): Any = toFn(value)
    }
  }
}
