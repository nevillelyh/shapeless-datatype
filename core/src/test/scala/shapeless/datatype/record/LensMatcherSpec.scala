package shapeless.datatype.record

import org.scalacheck.Prop.{BooleanOperators, all, forAll}
import org.scalacheck.Shapeless._
import org.scalacheck._
import shapeless._

class LensMatcherSpec extends Properties("LensMatcher") {

  import shapeless.datatype.test.Records._
  import shapeless.datatype.test.SerializableUtils._

  // always generate alphabet string for String
  implicit val arbString = Arbitrary(Gen.alphaStr)
  // always generate Some[T] for Option[T]
  implicit def arbOption[T](implicit arb: Arbitrary[T]) = Arbitrary(Gen.some(arb.arbitrary))
  // always generate non-empty List[T]
  implicit def arbList[T](implicit arb: Arbitrary[T]) = Arbitrary(Gen.nonEmptyListOf(arb.arbitrary))

  val mL = ensureSerializable(
    LensMatcher[Nested].on(_ >> 'longField)(math.abs(_) == math.abs(_)))
  val mML = ensureSerializable(
    LensMatcher[Nested].on(_ >> 'mixedField >> 'longField)(math.abs(_) == math.abs(_)))
  val mMulti = ensureSerializable(
    LensMatcher[Nested]
      .on(_ >> 'mixedField >> 'longField)(math.abs(_) == math.abs(_))
      .on(_ >> 'mixedField >> 'stringField)(_.toLowerCase == _.toLowerCase))

  val lensL = lens[Nested] >> 'longField
  val lensML = lens[Nested] >> 'mixedField >> 'longField
  val lensMS = lens[Nested] >> 'mixedField >> 'stringField

  property("root") = forAll { m: Nested =>
    all(
      "equal self"     |: mL(m, m),
      "equal negate"   |: mL(m, lensL.modify(m)(-_)),
      "not equal inc"  |: !mL(m, lensL.modify(m)(_ + 1L)),
      "not equal rest" |: !mL(m, lensML.modify(m)(_ + 1L))
    )
  }

  property("nested") = forAll { m: Nested =>
    all(
      "equal self"     |: mML(m, m),
      "equal negate"   |: mML(m, lensML.modify(m)(-_)),
      "not equal inc"  |: !mML(m, lensML.modify(m)(_ + 1L)),
      "not equal rest" |: !mML(m, lensL.modify(m)(_ + 1L))
    )
  }

  property("multi") = forAll { m: Nested =>
    all(
      "equal self"         |: mMulti(m, m),
      "equal negate"       |: mMulti(m, lensML.modify(m)(-_)),
      "equal upper"        |: mMulti(m, lensMS.modify(m)(_.toUpperCase)),
      "equal negate+upper" |: mMulti(m, lensMS.modify(lensML.modify(m)(-_))(_.toUpperCase)),
      "not equal inc"      |: !mMulti(m, lensML.modify(m)(_ + 1L)),
      "not equal append"   |: !mMulti(m, lensMS.modify(m)(_ + "!")),
      "not equal rest"     |: !mMulti(m, lensL.modify(m)(_ + 1L))
    )
  }

}
