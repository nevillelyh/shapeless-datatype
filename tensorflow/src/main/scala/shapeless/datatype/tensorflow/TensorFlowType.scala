package shapeless.datatype.tensorflow

import com.google.protobuf.ByteString
import org.tensorflow.example._
import shapeless._

import scala.collection.JavaConverters._

class TensorFlowType[A] extends Serializable {
  def fromExampleBuilder[L <: HList](m: Example.Builder)
                                    (implicit
                                     gen: LabelledGeneric.Aux[A, L],
                                     fromL: FromFeatures[L]): Option[A] =
    fromL(m.getFeaturesBuilder).map(gen.from)

  def fromExample[L <: HList](m: Example)
                             (implicit
                              gen: LabelledGeneric.Aux[A, L],
                              fromL: FromFeatures[L]): Option[A] =
    fromL(m.getFeatures.toBuilder).map(gen.from)

  def toExampleBuilder[L <: HList](a: A)
                                  (implicit
                                  gen: LabelledGeneric.Aux[A, L],
                                  toL: ToFeatures[L]): Example.Builder =
    Example.newBuilder().setFeatures(toL(gen.to(a)))

  def toExample[L <: HList](a: A)
                           (implicit
                            gen: LabelledGeneric.Aux[A, L],
                            toL: ToFeatures[L]): Example =
    Example.newBuilder().setFeatures(toL(gen.to(a))).build()
}

object TensorFlowType {
  def apply[A]: TensorFlowType[A] = new TensorFlowType[A]

  def at[V](fromFn: Feature => Seq[V], toFn: Seq[V] => Feature.Builder): BaseTensorFlowMappableType[V] =
    new BaseTensorFlowMappableType[V] {
      override def fromSeq(value: Feature): Seq[V] = fromFn(value)
      override def toSeq(value: Seq[V]): Feature = toFn(value).build()
    }

  def fromBooleans(xs: Seq[Boolean]): Feature.Builder =
    Feature.newBuilder().setInt64List(
      Int64List.newBuilder().addAllValue(xs.map(x => (if (x) 1L else 0L): java.lang.Long).asJava))
  def toBooleans(f: Feature): Seq[Boolean] =
    f.getInt64List.getValueList.asScala.map(x => if (x > 0) true else false).toSeq

  def fromLongs(xs: Seq[Long]): Feature.Builder =
    Feature.newBuilder().setInt64List(xs.foldLeft(Int64List.newBuilder())(_.addValue(_)).build())
  def toLongs(f: Feature): Seq[Long] =
    f.getInt64List.getValueList.asScala.toSeq.asInstanceOf[Seq[Long]]

  def fromInts(xs: Seq[Int]): Feature.Builder = fromLongs(xs.map(_.toLong))
  def toInts(f: Feature): Seq[Int] = toLongs(f).map(_.toInt)

  def fromFloats(xs: Seq[Float]): Feature.Builder =
    Feature.newBuilder().setFloatList(xs.foldLeft(FloatList.newBuilder())(_.addValue(_)).build())
  def toFloats(f: Feature): Seq[Float] =
    f.getFloatList.getValueList.asScala.toSeq.asInstanceOf[Seq[Float]]

  def fromDoubles(xs: Seq[Double]): Feature.Builder = fromFloats(xs.map(_.toFloat))
  def toDoubles(f: Feature): Seq[Double] = toFloats(f).map(_.toDouble)

  def fromByteStrings(xs: Seq[ByteString]): Feature.Builder =
    Feature.newBuilder().setBytesList(BytesList.newBuilder().addAllValue(xs.asJava))
  def toByteStrings(f: Feature): Seq[ByteString] = f.getBytesList.getValueList.asScala.toSeq

  def fromByteArrays(xs: Seq[Array[Byte]]): Feature.Builder = fromByteStrings(xs.map(ByteString.copyFrom))
  def toByteArrays(f: Feature): Seq[Array[Byte]] = toByteStrings(f).map(_.toByteArray)

  def fromStrings(xs: Seq[String]): Feature.Builder = fromByteStrings(xs.map(ByteString.copyFromUtf8))
  def toStrings(f: Feature): Seq[String] = toByteStrings(f).map(_.toStringUtf8)
}
