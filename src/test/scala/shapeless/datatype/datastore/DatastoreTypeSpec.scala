package shapeless.datatype.datastore

import org.scalacheck.Prop.forAll
import org.scalacheck.Shapeless._
import org.scalacheck._
import shapeless._
import shapeless.datatype.SerializableUtils
import shapeless.datatype.record._

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

class DatastoreTypeSpec extends Properties("DatastoreType") {

  import Records._

  implicit val compareByteArrays = (x: Array[Byte], y: Array[Byte]) => java.util.Arrays.equals(x, y)

  def roundTrip[A, L <: HList](m: A, t: DatastoreType[A] = DatastoreType[A])
                              (implicit
                               gen: LabelledGeneric.Aux[A, L],
                               fromL: FromEntity[L],
                               toL: ToEntity[L],
                               rm: RecordMatcher[L]): Boolean = {
    val rmt = RecordMatcherType[A]
    t.fromEntity(t.toEntity(m)).exists(rmt(_, m))
  }

  property("required") = forAll { m: Required => roundTrip(m) }
  property("optional") = forAll { m: Optional => roundTrip(m) }
  property("repeated") = forAll { m: Repeated => roundTrip(m) }
  property("mixed") = forAll { m: Mixed => roundTrip(m) }
  property("nested") = forAll { m: Nested => roundTrip(m) }

  val t = SerializableUtils.ensureSerializable(DatastoreType[Nested])
  property("serializable") = forAll { m: Nested => roundTrip(m, t) }

}
