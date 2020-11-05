package shapeless.datatype.bigquery

import java.net.URI

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.google.api.services.bigquery.model.TableRow
import com.google.common.io.BaseEncoding
import com.google.protobuf.ByteString
import org.joda.time.{Instant, LocalDate, LocalDateTime, LocalTime}
import org.scalacheck.Prop.forAll
import org.scalacheck._
import magnolify.scalacheck.auto._
import shapeless._
import shapeless.datatype.record._

import scala.reflect.runtime.universe._

object BigQueryTypeSpec extends Properties("BigQueryType") {
  import shapeless.datatype.test.Records._
  import shapeless.datatype.test.SerializableUtils._

  val mapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

  implicit def compareByteArrays(x: Array[Byte], y: Array[Byte]) = java.util.Arrays.equals(x, y)
  implicit def compareIntArrays(x: Array[Int], y: Array[Int]) = java.util.Arrays.equals(x, y)

  def roundTrip[A: TypeTag, L <: HList](m: A)(implicit
    gen: LabelledGeneric.Aux[A, L],
    fromL: FromTableRow[L],
    toL: ToTableRow[L],
    mr: MatchRecord[L]
  ): Boolean = {
    BigQuerySchema[A] // FIXME: verify the generated schema
    val t = ensureSerializable(BigQueryType[A])
    val f1: SerializableFunction[A, TableRow] =
      new SerializableFunction[A, TableRow] {
        override def apply(m: A): TableRow = t.toTableRow(m)
      }
    val f2: SerializableFunction[TableRow, Option[A]] =
      new SerializableFunction[TableRow, Option[A]] {
        override def apply(m: TableRow): Option[A] = t.fromTableRow(m)
      }
    val toFn = ensureSerializable(f1)
    val fromFn = ensureSerializable(f2)
    val copy = fromFn(mapper.readValue(mapper.writeValueAsString(toFn(m)), classOf[TableRow]))
    val rm = RecordMatcher[A]
    copy.exists(rm(_, m))
  }

  implicit val byteStringBigQueryMappableType = BigQueryType.at[ByteString]("BYTES")(
    x => ByteString.copyFrom(BaseEncoding.base64().decode(x.toString)),
    x => BaseEncoding.base64().encode(x.toByteArray)
  )
  property("required") = forAll { m: Required => roundTrip(m) }
  property("optional") = forAll { m: Optional => roundTrip(m) }
  property("repeated") = forAll { m: Repeated => roundTrip(m) }
  property("mixed") = forAll { m: Mixed => roundTrip(m) }
  property("nested") = forAll { m: Nested => roundTrip(m) }
  property("seqs") = forAll { m: Seqs => roundTrip(m) }

  implicit val arbDate = Arbitrary(arbInstant.arbitrary.map(i => new LocalDate(i.getMillis)))
  implicit val arbTime = Arbitrary(arbInstant.arbitrary.map(i => new LocalTime(i.getMillis)))
  implicit val arbDateTime = Arbitrary(
    arbInstant.arbitrary.map(i => new LocalDateTime(i.getMillis))
  )

  case class DateTimeTypes(
    instant: Instant,
    date: LocalDate,
    time: LocalTime,
    dateTime: LocalDateTime
  )
  property("date time types") = forAll { m: DateTimeTypes => roundTrip(m) }

  implicit val uriBigQueryType =
    BigQueryType.at[URI]("STRING")(v => URI.create(v.toString), _.toASCIIString)
  property("custom") = forAll { m: Custom => roundTrip(m) }
}
