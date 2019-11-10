package shapeless.datatype.tensorflow

import com.google.protobuf.ByteString
import org.tensorflow.example._
import shapeless.datatype.mappable.{BaseMappableType, MappableType}

trait BaseTensorFlowMappableType[V] extends MappableType[Features.Builder, V] {
  def from(value: Feature): V = fromSeq(value).head
  def to(value: V): Feature = toSeq(Seq(value))
  def fromSeq(value: Feature): Seq[V]
  def toSeq(value: Seq[V]): Feature

  override def get(m: Features.Builder, key: String): Option[V] =
    Option(m.getFeatureMap.get(key)).map(from)
  override def getAll(m: Features.Builder, key: String): Seq[V] =
    Option(m.getFeatureMap.get(key)).toSeq.flatMap(fromSeq)

  override def put(key: String, value: V, tail: Features.Builder): Features.Builder =
    tail.putFeature(key, to(value))
  override def put(key: String, value: Option[V], tail: Features.Builder): Features.Builder =
    value.foldLeft(tail)((b, v) => b.putFeature(key, to(v)))
  override def put(key: String, values: Seq[V], tail: Features.Builder): Features.Builder =
    tail.putFeature(key, toSeq(values))
}

trait TensorFlowMappableType {
  implicit val tensorFlowBaseMappableType = new BaseMappableType[Features.Builder] {
    override def base: Features.Builder = Features.newBuilder()
    override def get(m: Features.Builder, key: String): Option[Features.Builder] = ???
    override def getAll(m: Features.Builder, key: String): Seq[Features.Builder] = ???
    override def put(
      key: String,
      value: Features.Builder,
      tail: Features.Builder
    ): Features.Builder = ???
    override def put(
      key: String,
      value: Option[Features.Builder],
      tail: Features.Builder
    ): Features.Builder = ???
    override def put(
      key: String,
      values: Seq[Features.Builder],
      tail: Features.Builder
    ): Features.Builder = ???
  }

  import TensorFlowType._

  implicit val booleanTensorFlowMappableType = at[Boolean](toBooleans, fromBooleans)
  implicit val intTensorFlowMappableType = at[Int](toInts, fromInts)
  implicit val longTensorFlowMappableType = at[Long](toLongs, fromLongs)
  implicit val floatTensorFlowMappableType = at[Float](toFloats, fromFloats)
  implicit val doubleTensorFlowMappableType = at[Double](toDoubles, fromDoubles)
  implicit val byteStringTensorFlowMappableType = at[ByteString](toByteStrings, fromByteStrings)
  implicit val byteArrayTensorFlowMappableType = at[Array[Byte]](toByteArrays, fromByteArrays)
  implicit val stringTensorFlowMappableType = at[String](toStrings, fromStrings)
}
