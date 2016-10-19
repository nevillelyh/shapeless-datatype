package shapeless.datatype.test

import java.{io => jio}

object SerializableUtils {

  private def serializeToByteArray(value: Serializable): Array[Byte] = {
    val buffer = new jio.ByteArrayOutputStream()
    val oos = new jio.ObjectOutputStream(buffer)
    oos.writeObject(value)
    buffer.toByteArray
  }

  private def deserializeFromByteArray(encodedValue: Array[Byte]): AnyRef = {
    val ois = new jio.ObjectInputStream(new jio.ByteArrayInputStream(encodedValue))
    ois.readObject()
  }

  def ensureSerializable[T <: Serializable](value: T): T =
    deserializeFromByteArray(serializeToByteArray(value)).asInstanceOf[T]

}
