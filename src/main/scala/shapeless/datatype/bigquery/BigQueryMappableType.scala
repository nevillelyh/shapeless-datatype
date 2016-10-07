package shapeless.datatype.bigquery

import com.google.common.io.BaseEncoding
import shapeless.datatype.mappable.{BaseMappableType, MappableType}

import scala.collection.JavaConverters._

trait BaseBigQueryMappableType[V] extends MappableType[TableRow, V] {
  def from(value: AnyRef): V
  def to(value: V): AnyRef

  override def get(m: TableRow, key: String): Option[V] =
    Option(m.get(key)).map(from)
  override def getAll(m: TableRow, key: String): Seq[V] =
    if (m.containsKey(key))
      m.get(key).asInstanceOf[java.util.List[AnyRef]].asScala.map(from)
    else
      Nil

  override def put(key: String, value: V, tail: TableRow): TableRow = {
    tail.put(key, to(value))
    tail
  }
  override def put(key: String, value: Option[V], tail: TableRow): TableRow = {
    value.foreach(v => tail.put(key, to(v)))
    tail
  }
  override def put(key: String, values: Seq[V], tail: TableRow): TableRow = {
    tail.put(key, values.map(to).asJava)
    tail
  }
}

trait BigQueryMappableType {
  implicit val bigQueryBaseMappableType = new BaseMappableType[TableRow] {
    override def base: TableRow = new java.util.LinkedHashMap[String, AnyRef]()

    override def get(m: TableRow, key: String): Option[TableRow] =
      Option(m.get(key)).map(_.asInstanceOf[TableRow])
    override def getAll(m: TableRow, key: String): Seq[TableRow] =
      Option(m.get(key)).toSeq
        .flatMap(_.asInstanceOf[java.util.List[AnyRef]].asScala.map(_.asInstanceOf[TableRow]))

    override def put(key: String, value: TableRow, tail: TableRow): TableRow = {
      tail.put(key, value)
      tail
    }
    override def put(key: String, value: Option[TableRow], tail: TableRow): TableRow = {
      value.foreach(v => tail.put(key, v))
      tail
    }
    override def put(key: String, values: Seq[TableRow], tail: TableRow): TableRow = {
      tail.put(key, values.asJava)
      tail
    }
  }

  private def at[T](fromFn: AnyRef => T, toFn: T => AnyRef) = new BaseBigQueryMappableType[T] {
    override def from(value: AnyRef): T = fromFn(value)
    override def to(value: T): AnyRef = toFn(value)
  }

  private def id[T](x: T): AnyRef = x.asInstanceOf[AnyRef]
  implicit val intBigQueryMappableType = at[Int](_.toString.toInt, id)
  implicit val longBigQueryMappableType = at[Long](_.toString.toLong, id)
  implicit val floatBigQueryMappableType = at[Float](_.toString.toFloat, id)
  implicit val doubleBigQueryMappableType = at[Double](_.toString.toDouble, id)
  implicit val booleanBigQueryMappableType = at[Boolean](_.toString.toBoolean, id)
  implicit val stringBigQueryMappableType = at[String](_.toString, id)
  implicit val bytesBigQueryMappableType =
    at[Array[Byte]](x => BaseEncoding.base64().decode(x.toString), id)
  // FIXME: timestamp
}

object BigQueryMappableType extends BigQueryMappableType
