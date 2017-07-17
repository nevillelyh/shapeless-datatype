package shapeless.datatype.algebird

import com.google.protobuf.ByteString
import com.twitter.algebird.Semigroup
import com.twitter.algebird.macros.caseclass
import org.joda.time.Instant
import org.scalacheck.Prop.forAll
import org.scalacheck.Shapeless._
import org.scalacheck._
import shapeless._
import shapeless.datatype.record.{MatchRecord, RecordMatcher}

object AlgebirdSpec extends Properties("Algebird") {

  import shapeless.datatype.test.Records._

  implicit val byteStringSg = Semigroup.from[ByteString](_ concat _)
  implicit val byteArraySg = Semigroup.from[Array[Byte]](_ ++ _)
  implicit val intArraySg = Semigroup.from[Array[Int]](_ ++ _)
  implicit val intVectorSg = Semigroup.from[Vector[Int]](_ ++ _)
  implicit val instantSg = Semigroup.from[Instant] { (x, y) =>
    new Instant(x.getMillis + y.getMillis)
  }

  implicit def compareByteArrays(x: Array[Byte], y: Array[Byte]) = java.util.Arrays.equals(x, y)
  implicit def compareIntArrays(x: Array[Int], y: Array[Int]) = java.util.Arrays.equals(x, y)

  def test[A: Semigroup, L <: HList](x: A, y: A, expected: A)
                                    (implicit
                                     gen: LabelledGeneric.Aux[A, L],
                                     mr: MatchRecord[L]): Prop = {
    val sg = implicitly[Semigroup[A]]
    val rm = RecordMatcher[A]
    rm(sg.plus(x, y), expected)
  }

  property("required") = forAll { (x: Required, y: Required) =>
    test(x, y, caseclass.semigroup[Required].plus(x, y))
  }
  property("optional") = forAll { (x: Optional, y: Optional) =>
    test(x, y, caseclass.semigroup[Optional].plus(x, y))
  }
  property("repeated") = forAll { (x: Repeated, y: Repeated) =>
    test(x, y, caseclass.semigroup[Repeated].plus(x, y))
  }
  property("mixed") = forAll { (x: Mixed, y: Mixed) =>
    test(x, y, caseclass.semigroup[Mixed].plus(x, y))
  }
  property("nested") = forAll { (x: Nested, y: Nested) =>
    test(x, y, caseclass.semigroup[Nested].plus(x, y))
  }
  property("seqs") = forAll { (x: Seqs, y: Seqs) =>
    test(x, y, caseclass.semigroup[Seqs].plus(x, y))
  }

}
