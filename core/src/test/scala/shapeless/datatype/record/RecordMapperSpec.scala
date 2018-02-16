package shapeless.datatype.record

import org.scalacheck.Prop.forAll
import org.scalacheck.ScalacheckShapeless._
import org.scalacheck._
import shapeless._

import scala.language.implicitConversions

object RecordMapperRecords {
  case class RequiredA(intField: Int, longField: Long, stringField: String)
  case class RequiredB(intField: Int, longField: Long, stringField: Array[Byte])
  case class OptionalA(intField: Option[Int], longField: Option[Long], stringField: Option[String])
  case class OptionalB(intField: Option[Int], longField: Option[Long], stringField: Option[Array[Byte]])
  case class RepeatedA(intList: List[Int], longList: List[Long], stringList: List[String],
                       intSet: Set[Int], longSet: Set[Long], stringSet: Set[String],
                       intMap: Map[String, Int], longMap: Map[String, Long]/*, stringMap: Map[String, String]*/)
  case class RepeatedB(intList: List[Int], longList: List[Long], stringList: List[Array[Byte]],
                       intSet: Set[Int], longSet: Set[Long], stringSet: Set[Array[Byte]],
                       intMap: Map[String, Int], longMap: Map[String, Long]/*, stringMap: Map[String, Array[Byte]]*/)
  case class MixedA(intField: Int, stringField: String,
                    intFieldO: Option[Int], stringFieldO: Option[String],
                    intList: List[Int], stringList: List[String],
                    intSet: Set[Int], stringSet: Set[String],
                    intMap: Map[String, Int]/*, stringMap: Map[String, String]*/)
  case class MixedB(intField: Int, stringField: Array[Byte],
                    intFieldO: Option[Int], stringFieldO: Option[Array[Byte]],
                    intList: List[Int], stringList: List[Array[Byte]],
                    intSet: Set[Int], stringSet: Set[Array[Byte]],
                    intMap: Map[String, Int]/*, stringMap: Map[String, Array[Byte]]*/)
  case class NestedA(required: String, optional: Option[String],
                     list: List[String], set: Set[String], map: Map[String, Int],
                     requiredN: MixedA, optionalN: Option[MixedA],
                     listN: List[MixedA], setN: Set[MixedA]/*, mapN: Map[String, MixedA]*/)
  case class NestedB(required: Array[Byte], optional: Option[Array[Byte]],
                     list: List[Array[Byte]], set: Set[Array[Byte]], map: Map[String, Int],
                     requiredN: MixedB, optionalN: Option[MixedB],
                     listN: List[MixedB], setN: Set[MixedB]/*, mapN: Map[String, MixedB]*/)
}

object RecordMapperSpec extends Properties("RecordMapper") {

  import RecordMapperRecords._
  import shapeless.datatype.test.SerializableUtils._

  implicit def s2b(x: String): Array[Byte] = x.getBytes
  implicit def b2s(x: Array[Byte]): String = new String(x)

  class RoundTrip[B] {
    def from[A, LA <: HList, LB <: HList](a: A)
                                         (implicit
                                          genA: LabelledGeneric.Aux[A, LA],
                                          genB: LabelledGeneric.Aux[B, LB],
                                          mrA: MapRecord[LA, LB],
                                          mrB: MapRecord[LB, LA]): Boolean = {
      val t = ensureSerializable(RecordMapper[A, B])
      t.from(t.to(a)) == a
    }
  }
  def roundTripTo[B]: RoundTrip[B] = new RoundTrip[B]

  property("required") = forAll { m: RequiredA => roundTripTo[RequiredB].from(m) }
  property("optional") = forAll { m: OptionalA => roundTripTo[OptionalB].from(m) }
  property("repeated") = forAll { m: RepeatedA => roundTripTo[RepeatedB].from(m) }
  property("mixed") = forAll { m: MixedA => roundTripTo[MixedB].from(m) }
  property("nested") = forAll { m: NestedA => roundTripTo[NestedB].from(m) }

}
