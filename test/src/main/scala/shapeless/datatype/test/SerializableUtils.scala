package shapeless.datatype.test

import java.{io => jio}

object SerializableUtils {

  def serializeToByteArray(value: Serializable): Array[Byte] = {
    try {
      val buffer = new jio.ByteArrayOutputStream()
      val oos = new jio.ObjectOutputStream(buffer)
      oos.writeObject(value)
      buffer.toByteArray
    } catch {
      case e: jio.IOException =>
        throw new IllegalArgumentException(s"unable to serialize $value", e)
    }
  }

  def deserializeFromByteArray(encodedValue: Array[Byte], description: String): AnyRef = {
    try {
      val ois = new jio.ObjectInputStream(new jio.ByteArrayInputStream(encodedValue))
      ois.readObject()
    } catch {
      case e @ (_: jio.IOException | _: ClassNotFoundException) =>
        throw new IllegalArgumentException(s"unable to deserialize $description", e)
    }
  }

  def ensureSerializable[T <: Serializable](value: T): T =
    deserializeFromByteArray(serializeToByteArray(value), value.toString).asInstanceOf[T]

}
