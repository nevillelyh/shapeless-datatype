package shapeless.datatype.bigquery

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.google.api.services.bigquery.model.TableRow
import org.joda.time.{Instant, LocalDate, LocalDateTime, LocalTime}
import org.scalacheck.Prop.forAll
import org.scalacheck.Shapeless._
import org.scalacheck._
import shapeless._
import shapeless.datatype.record._

object BigQueryTypeSpec extends Properties("BigQueryType") {

  import shapeless.datatype.test.Records._
  import shapeless.datatype.test.SerializableUtils._

  val mapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

  implicit def compareByteArrays(x: Array[Byte], y: Array[Byte]) = java.util.Arrays.equals(x, y)
  implicit def compareIntArrays(x: Array[Int], y: Array[Int]) = java.util.Arrays.equals(x, y)

  def roundTrip[A, L <: HList](m: A)
                              (implicit
                               gen: LabelledGeneric.Aux[A, L],
                               fromL: FromTableRow[L],
                               toL: ToTableRow[L],
                               mr: MatchRecord[L]): Boolean = {
    val t = ensureSerializable(BigQueryType[A])
    val tr1 = t.toTableRow(m)
    val tr2 = mapper.readValue(mapper.writeValueAsString(tr1), classOf[TableRow])
    val rm = RecordMatcher[A]
    t.fromTableRow(tr2).exists(rm(_, m))
  }

  property("required") = forAll { m: Required => roundTrip(m) }
  property("optional") = forAll { m: Optional => roundTrip(m) }
  property("repeated") = forAll { m: Repeated => roundTrip(m) }
  property("mixed") = forAll { m: Mixed => roundTrip(m) }
  property("nested") = forAll { m: Nested => roundTrip(m) }
  property("seq types") = forAll { m: SeqTypes => roundTrip(m) }

  implicit val arbDate = Arbitrary(Gen.const(LocalDate.now()))
  implicit val arbTime = Arbitrary(Gen.const(LocalTime.now()))
  implicit val arbDateTime = Arbitrary(Gen.const(LocalDateTime.now()))

  case class DateTimeTypes(instant: Instant,
                           date: LocalDate,
                           time: LocalTime,
                           dateTime: LocalDateTime)
  property("date time types") = forAll { m: DateTimeTypes => roundTrip(m) }

}
