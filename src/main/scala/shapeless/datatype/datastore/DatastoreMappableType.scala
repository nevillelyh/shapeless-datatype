package shapeless.datatype.datastore

import com.google.datastore.v1.client.DatastoreHelper._
import com.google.datastore.v1.{Entity, Value}
import com.google.protobuf.{ByteString, Timestamp}
import org.joda.time.Instant
import org.joda.time.DateTimeConstants
import shapeless.datatype.mappable.{BaseMappableType, MappableType}

import scala.collection.JavaConverters._

trait BaseDatastoreMappableType[V] extends MappableType[Entity.Builder, V] {
  def from(value: Value): V
  def to(value: V): Value

  override def get(m: Entity.Builder, key: String): Option[V] =
    Option(m.getPropertiesMap.get(key)).map(from)
  override def getAll(m: Entity.Builder, key: String): Seq[V] =
    Option(m.getPropertiesMap.get(key)).toSeq
      .flatMap(_.getArrayValue.getValuesList.asScala.map(from))

  override def put(key: String, value: V, tail: Entity.Builder): Entity.Builder =
    tail.putProperties(key, to(value))
  override def put(key: String, value: Option[V], tail: Entity.Builder): Entity.Builder =
    value.foldLeft(tail)((b, v) => b.putProperties(key, to(v)))
  override def put(key: String, values: Seq[V], tail: Entity.Builder): Entity.Builder =
    tail.putProperties(key, makeValue(values.map(to).asJava).build())
}

trait DatastoreMappableType {
  implicit val datastoreBaseMappableType = new BaseMappableType[Entity.Builder] {
    override def base: Entity.Builder = Entity.newBuilder()

    override def get(m: Entity.Builder, key: String): Option[Entity.Builder] =
      Option(m.getPropertiesMap.get(key)).map(_.getEntityValue.toBuilder)
    override def getAll(m: Entity.Builder, key: String): Seq[Entity.Builder] =
      Option(m.getPropertiesMap.get(key)).toSeq
        .flatMap(_.getArrayValue.getValuesList.asScala.map(_.getEntityValue.toBuilder))

    override def put(key: String, value: Entity.Builder, tail: Entity.Builder): Entity.Builder =
      tail.putProperties(key, makeValue(value).build())
    override def put(key: String, value: Option[Entity.Builder], tail: Entity.Builder): Entity.Builder =
      value.foldLeft(tail)((b, v) => b.putProperties(key, makeValue(v).build()))
    override def put(key: String, values: Seq[Entity.Builder], tail: Entity.Builder): Entity.Builder =
      tail.putProperties(key, makeValue(values.map(v => makeValue(v).build()).asJava).build())
  }

  private def at[T](fromFn: Value => T, toFn: T => Value) = new BaseDatastoreMappableType[T] {
    override def from(value: Value): T = fromFn(value)
    override def to(value: T): Value = toFn(value)
  }

  implicit val booleanEntityMappableType =
    at[Boolean](_.getBooleanValue, makeValue(_).build())
  implicit val intDatastoreMappableType =
    at[Int](_.getIntegerValue.toInt, makeValue(_).build())
  implicit val longEntityMappableType =
    at[Long](_.getIntegerValue, makeValue(_).build())
  implicit val floatEntityMappableType =
    at[Float](_.getDoubleValue.toFloat, makeValue(_).build())
  implicit val doubleEntityMappableType =
    at[Double](_.getDoubleValue, makeValue(_).build())
  implicit val stringEntityMappableType =
    at[String](_.getStringValue, makeValue(_).build())
  implicit val byteStringEntityMappableType =
    at[ByteString](_.getBlobValue, makeValue(_).build())
  implicit val byteArrayEntityMappableType =
    at[Array[Byte]](_.getBlobValue.toByteArray, v => makeValue(ByteString.copyFrom(v)).build())
  implicit val timestampEntityMappableType = at[Instant](toInstant, fromInstant)

  private def toInstant(v: Value): Instant = {
    val t = v.getTimestampValue
    new Instant(t.getSeconds * DateTimeConstants.MILLIS_PER_SECOND + t.getNanos / 1000000)
  }
  private def fromInstant(i: Instant): Value = {
    val t = Timestamp.newBuilder()
      .setSeconds(i.getMillis / DateTimeConstants.MILLIS_PER_SECOND)
      .setNanos((i.getMillis % 1000).toInt * 1000000)
    Value.newBuilder().setTimestampValue(t).build()
  }

}

object DatastoreMappableType extends DatastoreMappableType
