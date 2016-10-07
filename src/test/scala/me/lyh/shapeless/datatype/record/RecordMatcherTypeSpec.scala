package me.lyh.shapeless.datatype.record

import me.lyh.shapeless.datatype.SerializableUtils
import org.scalacheck.Prop.{BooleanOperators, all, forAll}
import org.scalacheck.Shapeless._
import org.scalacheck._
import shapeless._
import shapeless.poly._

object RecordMatcherRecords {
  case class Required(intField: Int, doubleField: Double)
  case class Optional(intField: Option[Int], doubleField: Option[Double])
  case class Repeated(intField: List[Int], doubleField: List[Double])
  case class Mixed(intField: Int, doubleField: Double,
                   intFieldO: Option[Int], doubleFieldO: Option[Double],
                   intFieldR: List[Int], doubleFieldR: List[Double])
  case class Nested(required: Double, optional: Double, repeated: Double,
                    requiredN: Mixed, optionalN: Option[Mixed], repeatedN: List[Mixed])
}

class RecordMatcherTypeSpec extends Properties("RecordMatcherType") {

  import RecordMatcherRecords._

  // always generate [-π, π] for Double
  implicit val arbDouble = Arbitrary(Gen.chooseNum(-math.Pi, math.Pi))
  // always generate Some[T] for Option[T]
  implicit def arbOption[T](implicit arb: Arbitrary[T]) = Arbitrary(Gen.some(arb.arbitrary))
  // always generate non-empty List[T]
  implicit def arbList[T](implicit arb: Arbitrary[T]) = Arbitrary(Gen.nonEmptyListOf(arb.arbitrary))

  object negate extends ->((x: Double) => -x)
  object inc extends ->((x: Double) => x + 1.0)
  implicit val compareDoubles = (x: Double, y: Double) => math.abs(x) == math.abs(y)

  val t1 = RecordMatcherType[Required]
  val t2 = RecordMatcherType[Optional]
  val t3 = RecordMatcherType[Repeated]
  val t4 = RecordMatcherType[Mixed]
  val t5 = RecordMatcherType[Nested]

  property("required") = forAll { m: Required =>
    val m1 = everywhere(negate)(m)
    val m2 = everywhere(inc)(m)
    all(
      "equal self"    |: t1(m, m),
      "equal negate"  |: t1(m, m1),
      "not equal inc" |: !t1(m, m2))
  }

  property("optional") = forAll { m: Optional =>
    val m1 = everywhere(negate)(m)
    val m2 = everywhere(inc)(m)
    all(
      "equal self"    |: t2(m, m),
      "equal negate"  |: t2(m, m1),
      "not equal inc" |: !t2(m, m2))
  }

  property("repeated") = forAll { m: Repeated =>
    val m1 = everywhere(negate)(m)
    val m2 = everywhere(inc)(m)
    all(
      "equal self"    |: t3(m, m),
      "equal negate"  |: t3(m, m1),
      "not equal inc" |: !t3(m, m2))
  }

  property("mixed") = forAll { m: Mixed =>
    val m1 = everywhere(negate)(m)
    val m2 = everywhere(inc)(m)
    all(
      "equal self"    |: t4(m, m),
      "equal negate"  |: t4(m, m1),
      "not equal inc" |: !t4(m, m2))
  }

  property("nested") = forAll { m: Nested =>
    val m1 = everywhere(negate)(m)
    val m2 = everywhere(inc)(m)
    all(
      "equal self"    |: t5(m, m),
      "equal negate"  |: t5(m, m1),
      "not equal inc" |: !t5(m, m2))
  }

  val t = SerializableUtils.ensureSerializable(RecordMatcherType[Nested])
  property("serializable") = forAll { m: Nested =>
    val m1 = everywhere(negate)(m)
    val m2 = everywhere(inc)(m)
    all(
      "equal self"    |: t(m, m),
      "equal negate"  |: t(m, m1),
      "not equal inc" |: !t(m, m2))
  }

}
