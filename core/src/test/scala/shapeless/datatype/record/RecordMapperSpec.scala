package shapeless.datatype.record

import org.scalacheck.Prop.forAll
import org.scalacheck.Shapeless._
import org.scalacheck._
import shapeless._

import scala.language.implicitConversions

object RecordMapperRecords {
  case class RequiredA(intField: Int, longField: Long, stringField: String)
  case class RequiredB(intField: Int, longField: Long, stringField: Array[Byte])
  case class OptionalA(intField: Option[Int], longField: Option[Long], stringField: Option[String])
  case class OptionalB(intField: Option[Int], longField: Option[Long], stringField: Option[Array[Byte]])
  case class RepeatedA(intField: List[Int], longField: List[Long], stringField: List[String])
  case class RepeatedB(intField: List[Int], longField: List[Long], stringField: List[Array[Byte]])
  case class MixedA(intField: Int, stringField: String,
                    intFieldO: Option[Int], stringFieldO: Option[String],
                    intFieldR: List[Int], stringFieldR: List[String])
  case class MixedB(intField: Int, stringField: Array[Byte],
                    intFieldO: Option[Int], stringFieldO: Option[Array[Byte]],
                    intFieldR: List[Int], stringFieldR: List[Array[Byte]])
  case class NestedA(required: String, optional: Option[String], repeated: List[String],
                     requiredN: MixedA, optionalN: Option[MixedA], repeatedN: List[MixedA])
  case class NestedB(required: Array[Byte], optional: Option[Array[Byte]], repeated: List[Array[Byte]],
                     requiredN: MixedB, optionalN: Option[MixedB], repeatedN: List[MixedB])
}

class RecordMapperSpec extends Properties("RecordMapper") {

  import RecordMapperRecords._
  import shapeless.datatype.test.SerializableUtils._

  implicit def s2b(x: String): Array[Byte] = x.getBytes
  implicit def b2s(x: Array[Byte]): String = new String(x)

  class RoundTrip[B] {
    def from[A, LA <: HList, LB <: HList](a: A, t: RecordMapper[A, B] = RecordMapper[A, B])
                                         (implicit
                                          genA: LabelledGeneric.Aux[A, LA],
                                          genB: LabelledGeneric.Aux[B, LB],
                                          mrA: MapRecord[LA, LB],
                                          mrB: MapRecord[LB, LA]): Boolean =
      t.from(t.to(a)) == a
  }
  def roundTripTo[B]: RoundTrip[B] = new RoundTrip[B]

  property("required") = forAll { m: RequiredA => roundTripTo[RequiredB].from(m) }
  property("optional") = forAll { m: OptionalA => roundTripTo[OptionalB].from(m) }
  property("repeated") = forAll { m: RepeatedA => roundTripTo[RepeatedB].from(m) }
  property("mixed") = forAll { m: MixedA => roundTripTo[MixedB].from(m) }
  property("nested") = forAll { m: NestedA => roundTripTo[NestedB].from(m) }

  val t = ensureSerializable(RecordMapper[NestedA, NestedB])
  property("serializable") = forAll { m: NestedA => roundTripTo[NestedB].from(m, t) }

}
