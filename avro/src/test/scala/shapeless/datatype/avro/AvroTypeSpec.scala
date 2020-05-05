package shapeless.datatype.avro

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.net.URI
import java.nio.ByteBuffer

import com.google.protobuf.ByteString
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericDatumReader, GenericDatumWriter, GenericRecord}
import org.apache.avro.io.{DecoderFactory, EncoderFactory}
import org.joda.time.Instant
import org.scalacheck.Prop.forAll
import org.scalacheck.ScalacheckShapeless._
import org.scalacheck._
import shapeless._
import shapeless.datatype.record._

import scala.reflect.runtime.universe._

object AvroTypeSpec extends Properties("AvroType") {
  import shapeless.datatype.test.Records._
  import shapeless.datatype.test.SerializableUtils._

  implicit def compareByteArrays(x: Array[Byte], y: Array[Byte]) = java.util.Arrays.equals(x, y)
  implicit def compareIntArrays(x: Array[Int], y: Array[Int]) = java.util.Arrays.equals(x, y)

  def roundTrip[A: TypeTag, L <: HList](m: A)(implicit
    gen: LabelledGeneric.Aux[A, L],
    fromL: FromAvroRecord[L],
    toL: ToAvroRecord[L],
    mr: MatchRecord[L]
  ): Boolean = {
    val t = ensureSerializable(AvroType[A])
    val f1: SerializableFunction[A, GenericRecord] =
      new SerializableFunction[A, GenericRecord] {
        override def apply(m: A): GenericRecord = t.toGenericRecord(m)
      }
    val f2: SerializableFunction[GenericRecord, Option[A]] =
      new SerializableFunction[GenericRecord, Option[A]] {
        override def apply(m: GenericRecord): Option[A] = t.fromGenericRecord(m)
      }
    val toFn = ensureSerializable(f1)
    val fromFn = ensureSerializable(f2)
    val copy = fromFn(roundTripRecord(toFn(m)))
    val rm = RecordMatcher[A]
    copy.exists(rm(_, m))
  }

  def roundTripRecord(r: GenericRecord): GenericRecord = {
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

  implicit val byteStringAvroType = AvroType.at[ByteString](Schema.Type.BYTES)(
    v => ByteString.copyFrom(v.asInstanceOf[ByteBuffer]),
    v => ByteBuffer.wrap(v.toByteArray)
  )
  implicit val instantAvroType =
    AvroType.at[Instant](Schema.Type.LONG)(v => new Instant(v.asInstanceOf[Long]), _.getMillis)
  property("required") = forAll { m: Required => roundTrip(m) }
  property("optional") = forAll { m: Optional => roundTrip(m) }
  property("repeated") = forAll { m: Repeated => roundTrip(m) }
  property("mixed") = forAll { m: Mixed => roundTrip(m) }
  property("nested") = forAll { m: Nested => roundTrip(m) }
  property("seqs") = forAll { m: Seqs => roundTrip(m) }

  implicit val uriAvroType =
    AvroType.at[URI](Schema.Type.STRING)(v => URI.create(v.toString), _.toString)
  property("custom") = forAll { m: Custom => roundTrip(m) }
}
