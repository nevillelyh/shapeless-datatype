package shapeless.datatype

import org.apache.avro.specific.SpecificRecord
import shapeless._

import scala.language.experimental.macros

/*
TEST SCRIPT

import shapeless._
import shapeless.datatype.avro._

case class CRequired(i: Int, l: Long, f: Float, d: Double, b: Boolean, s: String)

DefaultSymbolicLabelling[CRequired]
DefaultSymbolicLabelling[Required]

Generic[CRequired]
Generic[Required]
 */

package object avro {
  implicit def mkAvroSymbolicLabelling[T <: SpecificRecord]: DefaultSymbolicLabelling[T] =
    macro AvroLabelledMacros.mkDefaultSymbolicLabellingImpl[T]

  implicit def materializeAvro[T <: SpecificRecord, R]: Generic.Aux[T, R] =
    macro AvroGenericMacros.materialize[T, R]
}
