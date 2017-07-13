package shapeless.datatype.tensorflow

import java.net.URI

import org.joda.time.Instant
import org.scalacheck.Prop.{all, forAll}
import org.scalacheck.Shapeless._
import org.scalacheck._
import org.tensorflow.example.{Feature, Int64List}
import shapeless._
import shapeless.datatype.record._

import scala.collection.JavaConverters._

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
    val rm = RecordMatcher[A]
    all(
      t.fromExample(t.toExample(m)).exists(rm(_, m)),
      t.fromExampleBuilder(t.toExampleBuilder(m)).exists(rm(_, m)))
  }

  implicit val timestampTensorFlowMappableType = TensorFlowType.at[Instant](
    TensorFlowType.toLongs(_).map(new Instant(_)),
    xs => TensorFlowType.fromLongs(xs.map(_.getMillis)))
  property("required") = forAll { m: Required => roundTrip(m) }
  property("optional") = forAll { m: Optional => roundTrip(m) }
  property("repeated") = forAll { m: Repeated => roundTrip(m) }
  property("mixed") = forAll { m: Mixed => roundTrip(m) }
  property("seq types") = forAll { m: SeqTypes => roundTrip(m) }

  implicit val uriTensorFlowType = TensorFlowType.at[URI](
    TensorFlowType.toStrings(_).map(URI.create),
    xs => TensorFlowType.fromStrings(xs.map(_.toString)))
  property("custom") = forAll { m: Custom => roundTrip(m)}

}
