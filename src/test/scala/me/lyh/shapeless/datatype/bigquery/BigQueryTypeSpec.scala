package me.lyh.shapeless.datatype.bigquery

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import me.lyh.shapeless.datatype.SerializableUtils
import me.lyh.shapeless.datatype.record._
import org.scalacheck.Prop.forAll
import org.scalacheck.Shapeless._
import org.scalacheck._
import shapeless._

object Records {
  case class Required(intField: Int, longField: Long, floatField: Float, doubleField: Double,
                      booleanField: Boolean, stringField: String,
                      bytesField: Array[Byte])
  case class Optional(intField: Option[Int], longField: Option[Long],
                      floatField: Option[Float], doubleField: Option[Double],
                      booleanField: Option[Boolean], stringField: Option[String],
                      bytesField: Option[Array[Byte]])
  case class Repeated(intField: List[Int], longField: List[Long],
                      floatField: List[Float], doubleField: List[Double],
                      booleanField: List[Boolean], stringField: List[String],
                      bytesField: List[Array[Byte]])
  case class Mixed(longField: Long, doubleField: Double,
                   booleanField: Boolean, stringField: String,
                   bytesField: Array[Byte],
                   longFieldO: Option[Long], doubleFieldO: Option[Double],
                   booleanFieldO: Option[Boolean], stringFieldO: Option[String],
                   bytesFieldO: Option[Array[Byte]],
                   longFieldR: List[Long], doubleFieldR: List[Double],
                   booleanFieldR: List[Boolean], stringFieldR: List[String],
                   bytesFieldR: List[Array[Byte]])
  case class Nested(required: Int, optional: Option[Int], repeated: List[Int],
                    requiredN: Mixed, optionalN: Option[Mixed], repeatedN: List[Mixed])
}

class BigQueryTypeSpec extends Properties("BigQueryType") {

  import Records._

  val mapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

  implicit val compareByteArrays =
    (x: Array[Byte], y: Array[Byte]) => java.util.Arrays.equals(x, y)

  def roundTrip[A, L <: HList](m: A, t: BigQueryType[A] = BigQueryType[A])
                              (implicit
                               gen: LabelledGeneric.Aux[A, L],
                               fromL: FromTableRow[L],
                               toL: ToTableRow[L],
                               rm: RecordMatcher[L]): Boolean = {
    val tr1 = t.toTableRow(m)
    val tr2 = mapper.readValue(mapper.writeValueAsString(tr1), classOf[TableRow])
    val rmt = RecordMatcherType[A]
    t.fromTableRow(tr2).exists(rmt(_, m))
  }

  property("required") = forAll { m: Required => roundTrip(m) }
  property("optional") = forAll { m: Optional => roundTrip(m) }
  property("repeated") = forAll { m: Repeated => roundTrip(m) }
  property("mixed") = forAll { m: Mixed => roundTrip(m) }
  property("nested") = forAll { m: Nested => roundTrip(m) }

  val t = SerializableUtils.ensureSerializable(BigQueryType[Nested])
  property("serializable") = forAll { m: Nested => roundTrip(m, t) }

}
