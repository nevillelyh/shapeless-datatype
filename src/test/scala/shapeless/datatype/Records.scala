package shapeless.datatype

import com.google.protobuf.ByteString
import org.joda.time.Instant

object Records {
  case class Required(booleanField: Boolean,
                      intField: Int, longField: Long, floatField: Float, doubleField: Double,
                      stringField: String,
                      byteStringField: ByteString, byteArrayField: Array[Byte],
                      timestampField: Instant)
  case class Optional(booleanField: Option[Boolean],
                      intField: Option[Int], longField: Option[Long],
                      floatField: Option[Float], doubleField: Option[Double],
                      stringField: Option[String],
                      byteStringField: Option[ByteString], byteArrayField: Option[Array[Byte]],
                      timestampField: Option[Instant])
  case class Repeated(booleanField: List[Boolean],
                      intField: List[Int], longField: List[Long],
                      floatField: List[Float], doubleField: List[Double],
                      stringField: List[String],
                      byteStringField: List[ByteString], byteArrayField: List[Array[Byte]],
                      timestampField: List[Instant])
  case class Mixed(booleanField: Boolean,
                   longField: Long, doubleField: Double,
                   stringField: String,
                   byteStringField: ByteString, byteArrayField: Array[Byte],
                   timestampField: Instant,
                   booleanFieldO: Option[Boolean],
                   longFieldO: Option[Long], doubleFieldO: Option[Double],
                   stringFieldO: Option[String],
                   byteStringFieldO: Option[ByteString], byteArrayFieldO: Option[Array[Byte]],
                   timestampFieldO: Option[Instant],
                   booleanFieldR: List[Boolean],
                   longFieldR: List[Long], doubleFieldR: List[Double],
                   stringFieldR: List[String],
                   byteStringFieldR: List[ByteString], byteArrayFieldR: List[Array[Byte]],
                   timestampFieldR: List[Instant])
  case class Nested(required: Int, optional: Option[Int], repeated: List[Int],
                    requiredN: Mixed, optionalN: Option[Mixed], repeatedN: List[Mixed])
  case class SeqTypes(array: Array[Int], list: List[Int], vector: Vector[Int])
}
