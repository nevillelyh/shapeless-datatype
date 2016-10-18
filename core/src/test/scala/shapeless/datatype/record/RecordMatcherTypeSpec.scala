package shapeless.datatype.record

import org.scalacheck.Prop.{BooleanOperators, all, forAll}
import org.scalacheck.Shapeless._
import org.scalacheck._
import shapeless._
import shapeless.datatype.SerializableUtils
import shapeless.poly._

class RecordMatcherTypeSpec extends Properties("RecordMatcherType") {

  import shapeless.datatype.Records._

  // always generate [-π, π] for Double
  implicit val arbDouble = Arbitrary(Gen.chooseNum(-math.Pi, math.Pi))
  // always generate Some[T] for Option[T]
  implicit def arbOption[T](implicit arb: Arbitrary[T]) = Arbitrary(Gen.some(arb.arbitrary))
  // always generate non-empty List[T]
  implicit def arbList[T](implicit arb: Arbitrary[T]) = Arbitrary(Gen.nonEmptyListOf(arb.arbitrary))

  object negate extends ->((x: Double) => -x)
  object inc extends ->((x: Double) => x + 1.0)
  implicit def compareDoubles(x: Double, y: Double) = math.abs(x) == math.abs(y)

  def test[A, L <: HList](original: A, withNegate: A, withInc: A,
                          t: RecordMatcherType[A] = RecordMatcherType[A])
                         (implicit
                          gen: LabelledGeneric.Aux[A, L],
                          rm: RecordMatcher[L]): Prop = {
    all(
      "equal self"    |: t(original, original),
      "equal negate"  |: t(original, withNegate),
      "not equal inc" |: !t(original, withInc))
  }

  property("required") = forAll { m: Required => test(m, everywhere(negate)(m), everywhere(inc)(m)) }
  property("optional") = forAll { m: Optional => test(m, everywhere(negate)(m), everywhere(inc)(m)) }
  property("repeated") = forAll { m: Repeated => test(m, everywhere(negate)(m), everywhere(inc)(m)) }
  property("mixed") = forAll { m: Mixed => test(m, everywhere(negate)(m), everywhere(inc)(m)) }
  property("nested") = forAll { m: Nested => test(m, everywhere(negate)(m), everywhere(inc)(m)) }

  val t = SerializableUtils.ensureSerializable(RecordMatcherType[Nested])
  property("serializable") = forAll { m: Nested => test(m, everywhere(negate)(m), everywhere(inc)(m)) }

}
