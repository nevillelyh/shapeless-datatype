package shapeless.datatype.tensorflow

import java.net.URI

import org.joda.time.Instant
import org.scalacheck.Prop.{all, forAll}
import org.scalacheck.ScalacheckShapeless._
import org.scalacheck._
import org.tensorflow.example.Example
import shapeless._
import shapeless.datatype.record._

object TensorFlowTypeSpec extends Properties("TensorFlowType") {

  import shapeless.datatype.test.Records._
  import shapeless.datatype.test.SerializableUtils._

  implicit def compareByteArrays(x: Array[Byte], y: Array[Byte]) = java.util.Arrays.equals(x, y)
  implicit def compareIntArrays(x: Array[Int], y: Array[Int]) = java.util.Arrays.equals(x, y)
  implicit def compareDouble(x: Double, y: Double) = x.toFloat == y.toFloat

  def roundTrip[A, L <: HList](m: A)
                              (implicit
                               gen: LabelledGeneric.Aux[A, L],
                               fromL: FromFeatures[L],
                               toL: ToFeatures[L],
                               mr: MatchRecord[L]): Prop = {
    val t = ensureSerializable(TensorFlowType[A])
    val f1: SerializableFunction[A, Example] =
      new SerializableFunction[A, Example] {
        override def apply(m: A): Example = t.toExample(m)
      }
    val f2: SerializableFunction[Example, Option[A]] =
      new SerializableFunction[Example, Option[A]] {
        override def apply(m: Example): Option[A] = t.fromExample(m)
      }
    val f3: SerializableFunction[A, Example.Builder] =
      new SerializableFunction[A, Example.Builder] {
        override def apply(m: A): Example.Builder = t.toExampleBuilder(m)
      }
    val f4: SerializableFunction[Example.Builder, Option[A]] =
      new SerializableFunction[Example.Builder, Option[A]] {
        override def apply(m: Example.Builder): Option[A] = t.fromExampleBuilder(m)
      }
    val toFn1 = ensureSerializable(f1)
    val fromFn1 = ensureSerializable(f2)
    val toFn2 = ensureSerializable(f3)
    val fromFn2 = ensureSerializable(f4)
    val copy1 = fromFn1(toFn1(m))
    val copy2 = fromFn2(toFn2(m))
    val rm = RecordMatcher[A]
    all(
      copy1.exists(rm(_, m)),
      copy2.exists(rm(_, m)))
  }

  implicit val timestampTensorFlowMappableType = TensorFlowType.at[Instant](
    TensorFlowType.toLongs(_).map(new Instant(_)),
    xs => TensorFlowType.fromLongs(xs.map(_.getMillis)))
  property("required") = forAll { m: Required => roundTrip(m) }
  property("optional") = forAll { m: Optional => roundTrip(m) }
  property("repeated") = forAll { m: Repeated => roundTrip(m) }
  property("mixed") = forAll { m: Mixed => roundTrip(m) }
  property("seqs") = forAll { m: Seqs => roundTrip(m) }

  implicit val uriTensorFlowType = TensorFlowType.at[URI](
    TensorFlowType.toStrings(_).map(URI.create),
    xs => TensorFlowType.fromStrings(xs.map(_.toString)))
  property("custom") = forAll { m: Custom => roundTrip(m)}

}
