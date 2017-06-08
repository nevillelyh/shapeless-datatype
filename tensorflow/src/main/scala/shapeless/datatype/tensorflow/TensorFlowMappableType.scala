package shapeless.datatype.tensorflow

import java.{lang => jl}

import com.google.protobuf.ByteString
import org.tensorflow.example._
import shapeless.datatype.mappable.{BaseMappableType, MappableType}

import scala.collection.JavaConverters._

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
    override def put(key: String, value: Features.Builder, tail: Features.Builder): Features.Builder = ???
    override def put(key: String, value: Option[Features.Builder], tail: Features.Builder): Features.Builder = ???
    override def put(key: String, values: Seq[Features.Builder], tail: Features.Builder): Features.Builder = ???
  }

  private def at[T](fromFn: Feature => Seq[T], toFn: (Feature.Builder, Seq[T]) => Feature.Builder) = new BaseTensorFlowMappableType[T] {
    override def fromSeq(value: Feature): Seq[T] = fromFn(value)
    override def toSeq(value: Seq[T]): Feature = toFn(Feature.newBuilder(), value).build()
  }

  implicit val booleanTensorFlowMappableType =
    at[Boolean](
      _.getInt64List.getValueList.asScala.map(toBoolean),
      (b, xs) => b.setInt64List(Int64List.newBuilder().addAllValue(xs.map(toLong).asJava)))
  implicit val intTensorFlowMappableType =
    at[Int](
      _.getInt64List.getValueList.asScala.map(_.toInt),
      (b, xs) => b.setInt64List(Int64List.newBuilder().addAllValue(xs.map(_.toLong.asInstanceOf[jl.Long]).asJava)))
  implicit val longTensorFlowMappableType =
    at[Long](
      _.getInt64List.getValueList.asScala.asInstanceOf[Seq[Long]],
      (b, xs) => b.setInt64List(Int64List.newBuilder().addAllValue(xs.asInstanceOf[Seq[jl.Long]].asJava)))
  implicit val floatTensorFlowMappableType =
    at[Float](
      _.getFloatList.getValueList.asScala.asInstanceOf[Seq[Float]],
      (b, xs) => b.setFloatList(FloatList.newBuilder().addAllValue(xs.asInstanceOf[Seq[jl.Float]].asJava)))
  implicit val doubleTensorFlowMappableType =
    at[Double](
      _.getFloatList.getValueList.asScala.map(_.toDouble),
      (b, xs) => b.setFloatList(FloatList.newBuilder().addAllValue(xs.map(_.toFloat.asInstanceOf[jl.Float]).asJava)))
  implicit val stringTensorFlowMappableType =
    at[String](
      _.getBytesList.getValueList.asScala.map(_.toStringUtf8),
      (b, xs) => b.setBytesList(BytesList.newBuilder().addAllValue(xs.map(ByteString.copyFromUtf8).asJava)))
  implicit val byteStringTensorFlowMappableType =
    at[ByteString](
      _.getBytesList.getValueList.asScala,
      (b, xs) => b.setBytesList(BytesList.newBuilder().addAllValue(xs.asJava)))
  implicit val byteArrayTensorFlowMappableType =
    at[Array[Byte]](
      _.getBytesList.getValueList.asScala.map(_.toByteArray),
      (b, xs) => b.setBytesList(BytesList.newBuilder().addAllValue(xs.map(ByteString.copyFrom).asJava)))

  private def toBoolean(l: jl.Long): Boolean = if (l > 0) true else false
  private def toLong(b: Boolean): jl.Long = if (b) 1L else 0L

}

object TensorFlowMappableType extends TensorFlowMappableType
