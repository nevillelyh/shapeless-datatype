package shapeless.datatype.bigquery

import com.google.common.io.BaseEncoding
import com.google.protobuf.ByteString
import org.joda.time._
import org.joda.time.format.{DateTimeFormat, DateTimeFormatterBuilder}
import shapeless.datatype.mappable.{BaseMappableType, MappableType}

import scala.collection.JavaConverters._

trait BaseBigQueryMappableType[V] extends MappableType[TableRow, V] {
  def from(value: Any): V
  def to(value: V): Any

  override def get(m: TableRow, key: String): Option[V] =
    Option(m.get(key)).map(from)
  override def getAll(m: TableRow, key: String): Seq[V] =
    if (m.containsKey(key))
      m.get(key).asInstanceOf[java.util.List[Any]].asScala.map(from)
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
    override def base: TableRow = new java.util.LinkedHashMap[String, Any]()

    override def get(m: TableRow, key: String): Option[TableRow] =
      Option(m.get(key)).map(_.asInstanceOf[TableRow])
    override def getAll(m: TableRow, key: String): Seq[TableRow] =
      Option(m.get(key)).toSeq
        .flatMap(_.asInstanceOf[java.util.List[TableRow]].asScala)

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

  private def at[T](fromFn: Any => T, toFn: T => Any) = new BaseBigQueryMappableType[T] {
    override def from(value: Any): T = fromFn(value)
    override def to(value: T): Any = toFn(value)
  }

  private def id[T](x: T): Any = x.asInstanceOf[Any]
  implicit val booleanBigQueryMappableType = at[Boolean](_.toString.toBoolean, id)
  implicit val intBigQueryMappableType = at[Int](_.toString.toInt, id)
  implicit val longBigQueryMappableType = at[Long](_.toString.toLong, id)
  implicit val floatBigQueryMappableType = at[Float](_.toString.toFloat, id)
  implicit val doubleBigQueryMappableType = at[Double](_.toString.toDouble, id)
  implicit val stringBigQueryMappableType = at[String](_.toString, id)
  implicit val byteStringBigQueryMappableType = at[ByteString](
    x => ByteString.copyFrom(BaseEncoding.base64().decode(x.toString)),
    x => BaseEncoding.base64().encode(x.toByteArray))
  implicit val byteArrayBigQueryMappableType = at[Array[Byte]](
    x => BaseEncoding.base64().decode(x.toString),
    x => BaseEncoding.base64().encode(x))

  import TimestampConverter._
  implicit val timestampBigQueryMappableType = at[Instant](toInstant, fromInstant)
  implicit val localDateBigQueryMappableType = at[LocalDate](toLocalDate, fromLocalDate)
  implicit val localTimeBigQueryMappableType = at[LocalTime](toLocalTime, fromLocalTime)
  implicit val localDateTimeBigQueryMappableType =
    at[LocalDateTime](toLocalDateTime, fromLocalDateTime)

}

private object TimestampConverter {

  // FIXME: verify that these match BigQuery specification
  // TIMESTAMP
  // YYYY-[M]M-[D]D[ [H]H:[M]M:[S]S[.DDDDDD]][time zone]
  private val timestampPrinter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS ZZZ")
  private val timestampParser = new DateTimeFormatterBuilder()
    .append(DateTimeFormat.forPattern("yyyy-MM-dd"))
    .appendOptional(new DateTimeFormatterBuilder()
      .append(DateTimeFormat.forPattern(" HH:mm:ss").getParser)
      .appendOptional(DateTimeFormat.forPattern(".SSSSSS").getParser)
      .toParser)
    .appendOptional(new DateTimeFormatterBuilder()
      .append(null, Array(" ZZZ", "ZZ").map(p => DateTimeFormat.forPattern(p).getParser))
      .toParser)
    .toFormatter
    .withZoneUTC()

  // DATE
  // YYYY-[M]M-[D]D
  private val datePrinter = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC()
  private val dateParser = datePrinter

  // TIME
  // [H]H:[M]M:[S]S[.DDDDDD]
  private val timePrinter = DateTimeFormat.forPattern("HH:mm:ss.SSSSSS").withZoneUTC()
  private val timeParser = new DateTimeFormatterBuilder()
    .append(DateTimeFormat.forPattern("HH:mm:ss").getParser)
    .appendOptional(DateTimeFormat.forPattern(".SSSSSS").getParser)
    .toFormatter
    .withZoneUTC()

  // DATETIME
  // YYYY-[M]M-[D]D[ [H]H:[M]M:[S]S[.DDDDDD]]
  private val datetimePrinter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
  private val datetimeParser = new DateTimeFormatterBuilder()
    .append(DateTimeFormat.forPattern("yyyy-MM-dd"))
    .appendOptional(new DateTimeFormatterBuilder()
      .append(DateTimeFormat.forPattern(" HH:mm:ss").getParser)
      .appendOptional(DateTimeFormat.forPattern(".SSSSSS").getParser)
      .toParser)
    .toFormatter
    .withZoneUTC()

  def toInstant(v: Any): Instant = timestampParser.parseDateTime(v.toString).toInstant
  def fromInstant(i: Instant): Any = timestampPrinter.print(i)

  def toLocalDate(v: Any): LocalDate = dateParser.parseLocalDate(v.toString)
  def fromLocalDate(d: LocalDate): Any = datePrinter.print(d)

  def toLocalTime(v: Any): LocalTime = timeParser.parseLocalTime(v.toString)
  def fromLocalTime(t: LocalTime): Any = timePrinter.print(t)

  def toLocalDateTime(v: Any): LocalDateTime = datetimeParser.parseLocalDateTime(v.toString)
  def fromLocalDateTime(dt: LocalDateTime): Any = datetimePrinter.print(dt)

}

object BigQueryMappableType extends BigQueryMappableType
