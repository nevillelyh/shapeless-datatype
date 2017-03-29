package shapeless.datatype.datastore

import org.scalacheck.Prop.{all, forAll}
import org.scalacheck.Shapeless._
import org.scalacheck._
import shapeless._
import shapeless.datatype.record._

class DatastoreTypeSpec extends Properties("DatastoreType") {

  import shapeless.datatype.test.Records._
  import shapeless.datatype.test.SerializableUtils._

  implicit def compareByteArrays(x: Array[Byte], y: Array[Byte]) = java.util.Arrays.equals(x, y)
  implicit def compareIntArrays(x: Array[Int], y: Array[Int]) = java.util.Arrays.equals(x, y)

  def roundTrip[A, L <: HList](m: A)
                              (implicit
                               gen: LabelledGeneric.Aux[A, L],
                               fromL: FromEntity[L],
                               toL: ToEntity[L],
                               mr: MatchRecord[L]): Prop = {
    val t = ensureSerializable(DatastoreType[A])
    val rm = RecordMatcher[A]
    all(
      t.fromEntity(t.toEntity(m)).exists(rm(_, m)),
      t.fromEntityBuilder(t.toEntityBuilder(m)).exists(rm(_, m))
    )
  }

  property("required") = forAll { m: Required => roundTrip(m) }
  property("optional") = forAll { m: Optional => roundTrip(m) }
  property("repeated") = forAll { m: Repeated => roundTrip(m) }
  property("mixed") = forAll { m: Mixed => roundTrip(m) }
  property("nested") = forAll { m: Nested => roundTrip(m) }
  property("seq types") = forAll { m: SeqTypes => roundTrip(m) }

}
