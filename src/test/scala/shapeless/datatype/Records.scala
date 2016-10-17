package shapeless.datatype

import com.google.protobuf.ByteString

object Records {
  case class Required(booleanField: Boolean,
                      intField: Int, longField: Long, floatField: Float, doubleField: Double,
                      stringField: String,
                      byteStringField: ByteString, byteArrayField: Array[Byte])
  case class Optional(booleanField: Option[Boolean],
                      intField: Option[Int], longField: Option[Long],
                      floatField: Option[Float], doubleField: Option[Double],
                      stringField: Option[String],
                      byteStringField: Option[ByteString], byteArrayField: Option[Array[Byte]])
  case class Repeated(booleanField: List[Boolean],
                      intField: List[Int], longField: List[Long],
                      floatField: List[Float], doubleField: List[Double],
                      stringField: List[String],
                      byteStringField: List[ByteString], byteArrayField: List[Array[Byte]])
  case class Mixed(booleanField: Boolean,
                   longField: Long, doubleField: Double,
                   stringField: String,
                   byteStringField: ByteString, byteArrayField: Array[Byte],
                   booleanFieldO: Option[Boolean],
                   longFieldO: Option[Long], doubleFieldO: Option[Double],
                   stringFieldO: Option[String],
                   byteStringFieldO: Option[ByteString], byteArrayFieldO: Option[Array[Byte]],
                   booleanFieldR: List[Boolean],
                   longFieldR: List[Long], doubleFieldR: List[Double],
                   stringFieldR: List[String],
                   byteStringFieldR: List[ByteString], byteArrayFieldR: List[Array[Byte]])
  case class Nested(required: Int, optional: Option[Int], repeated: List[Int],
                    requiredN: Mixed, optionalN: Option[Mixed], repeatedN: List[Mixed])
}
