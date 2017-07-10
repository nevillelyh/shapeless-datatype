package shapeless.datatype.avro

import java.nio.ByteBuffer

import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}
import shapeless.datatype.mappable.{BaseMappableType, MappableType}

import scala.collection.mutable.{Map => MMap}
import scala.collection.JavaConverters._

trait BaseAvroMappableType[V] extends MappableType[AvroRecord, V] {
  def from(value: Any): V
  def to(value: V): Any

  override def get(m: AvroRecord, key: String): Option[V] =
    Option(m.right.get.get(key)).map(from)
  override def getAll(m: AvroRecord, key: String): Seq[V] = m.right.get.get(key) match {
    case null => Nil
    case v => v.asInstanceOf[java.util.List[Any]].asScala.map(from)
  }

  override def put(key: String, value: V, tail: AvroRecord): AvroRecord = {
    tail.left.get.put(key, to(value))
    tail
  }
  override def put(key: String, value: Option[V], tail: AvroRecord): AvroRecord = {
    value.foreach(v => tail.left.get.put(key, to(v)))
    tail
  }
  override def put(key: String, values: Seq[V], tail: AvroRecord): AvroRecord = {
    tail.left.get.put(key, values.map(to).asJava)
    tail
  }
}

trait AvroMappableType {
  implicit val avroBaseMappableType = new BaseMappableType[AvroRecord] {
    override def base: AvroRecord = Left(AvroBuilder())

    override def get(m: AvroRecord, key: String): Option[AvroRecord] =
      Option(m.right.get.get(key)).map(v => Right(v.asInstanceOf[GenericRecord]))
    override def getAll(m: AvroRecord, key: String): Seq[AvroRecord] =
      Option(m.right.get.get(key)).toSeq
        .flatMap(_.asInstanceOf[java.util.List[GenericRecord]].asScala.map(Right(_)))

    override def put(key: String, value: AvroRecord, tail: AvroRecord): AvroRecord = {
      tail.left.get.put(key, value.left.get)
      tail
    }
    override def put(key: String, value: Option[AvroRecord], tail: AvroRecord): AvroRecord = {
      value.foreach(v => tail.left.get.put(key, v.left.get))
      tail
    }
    override def put(key: String, values: Seq[AvroRecord], tail: AvroRecord): AvroRecord = {
      tail.left.get.put(key, values.map(_.left.get))
      tail
    }
  }

  private def at[T] = new BaseAvroMappableType[T] {
    override def from(value: Any): T = value.asInstanceOf[T]
    override def to(value: T): Any = value.asInstanceOf[Any]
  }

  implicit val booleanAvroMappableType = at[Boolean]
  implicit val intAvroMappableType = at[Int]
  implicit val longAvroMappableType = at[Long]
  implicit val floatAvroMappableType = at[Float]
  implicit val doubleAvroMappableType = at[Double]
  implicit val stringAvroMappableType = new BaseAvroMappableType[String] {
    override def from(value: Any): String = value.toString
    override def to(value: String): Any = value.asInstanceOf[Any]
  }
  implicit val byteArrayAvroMappableType = new BaseAvroMappableType[Array[Byte]] {
    override def from(value: Any): Array[Byte] = {
      val bb = value.asInstanceOf[ByteBuffer]
      java.util.Arrays.copyOfRange(bb.array(), bb.position(), bb.limit())
    }
    override def to(value: Array[Byte]): Any = ByteBuffer.wrap(value)
  }

}

case class AvroBuilder private (m: MMap[String, Any] = MMap.empty) {
  def put(key: String, value: Any): Unit = m += (key -> value)
  def build(schema: Schema): GenericRecord = {
    val r = new GenericData.Record(schema)
    schema.getFields.asScala.foreach { f =>
      val key = f.name()
      if (m.contains(key)) {
        val s = f.schema()
        val v = if (s.getType == Schema.Type.RECORD) {
          m(key).asInstanceOf[AvroBuilder].build(s)
        } else if (s.getType == Schema.Type.UNION && s.getTypes.get(1).getType == Schema.Type.RECORD) {
          m(key).asInstanceOf[AvroBuilder].build(s.getTypes.get(1))
        } else if (s.getType == Schema.Type.ARRAY && s.getElementType.getType == Schema.Type.RECORD) {
          m(key).asInstanceOf[Seq[AvroBuilder]].map(_.build(s.getElementType)).asJava
        } else {
          m(key)
        }
        r.put(key, v)
      }
    }
    r
  }
}
