package shapeless.datatype.avro

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import org.apache.avro.generic.{GenericDatumReader, GenericDatumWriter, GenericRecord}
import org.apache.avro.io.{DecoderFactory, EncoderFactory}
import org.scalacheck.Prop.forAll
import org.scalacheck.Shapeless._
import org.scalacheck._
import shapeless._
import shapeless.datatype.record._

import scala.reflect.runtime.universe._

object AvroTypeSpec extends Properties("AvroType") {

  import shapeless.datatype.test.SerializableUtils._

  implicit def compareByteArrays(x: Array[Byte], y: Array[Byte]) = java.util.Arrays.equals(x, y)
  implicit def compareIntArrays(x: Array[Int], y: Array[Int]) = java.util.Arrays.equals(x, y)

  def roundTrip[A: TypeTag, L <: HList](m: A)
                              (implicit
                               gen: LabelledGeneric.Aux[A, L],
                               fromL: FromAvroRecord[L],
                               toL: ToAvroRecord[L],
                               mr: MatchRecord[L]): Boolean = {
    val t = ensureSerializable(AvroType[A])
    val rm = RecordMatcher[A]
    t.fromGenericRecord(roundTrip(t.toGenericRecord(m))).exists(rm(_, m))
  }

  def roundTrip(r: GenericRecord): GenericRecord = {
    val writer = new GenericDatumWriter[GenericRecord](r.getSchema)
    val baos = new ByteArrayOutputStream()
    val encoder = EncoderFactory.get().binaryEncoder(baos, null)
    writer.write(r, encoder)
    encoder.flush()
    baos.close()
    val bytes = baos.toByteArray

    val reader = new GenericDatumReader[GenericRecord](r.getSchema)
    val bais = new ByteArrayInputStream(bytes)
    val decoder = DecoderFactory.get().binaryDecoder(bais, null)
    reader.read(null, decoder)
  }

  property("required") = forAll { m: Required => roundTrip(m) }
  property("optional") = forAll { m: Optional => roundTrip(m) }
  property("repeated") = forAll { m: Repeated => roundTrip(m) }
  property("mixed") = forAll { m: Mixed => roundTrip(m) }
  property("nested") = forAll { m: Nested => roundTrip(m) }
  property("seq types") = forAll { m: SeqTypes => roundTrip(m) }

}

case class Required(booleanField: Boolean,
                    intField: Int, longField: Long, floatField: Float, doubleField: Double,
                    stringField: String, byteArrayField: Array[Byte])
case class Optional(booleanField: Option[Boolean],
                    intField: Option[Int], longField: Option[Long],
                    floatField: Option[Float], doubleField: Option[Double],
                    stringField: Option[String], byteArrayField: Option[Array[Byte]])
case class Repeated(booleanField: List[Boolean],
                    intField: List[Int], longField: List[Long],
                    floatField: List[Float], doubleField: List[Double],
                    stringField: List[String], byteArrayField: List[Array[Byte]])
case class Mixed(longField: Long, doubleField: Double, stringField: String,
                 longFieldO: Option[Long], doubleFieldO: Option[Double], stringFieldO: Option[String],
                 longFieldR: List[Long], doubleFieldR: List[Double], stringFieldR: List[String])
case class Nested(longField: Long, longFieldO: Option[Long], longFieldR: List[Long],
                  mixedField: Mixed, mixedFieldO: Option[Mixed], mixedFieldR: List[Mixed])
case class SeqTypes(array: Array[Int], list: List[Int], vector: Vector[Int])
