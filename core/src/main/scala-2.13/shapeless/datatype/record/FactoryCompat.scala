package shapeless.datatype.record

import scala.collection.{Factory, mutable}

// From https://github.com/pureconfig/pureconfig
trait FactoryCompat[-A, +C] {
  def newBuilder: mutable.Builder[A, C]
}

object FactoryCompat {
  implicit def fromFactory[A, C](implicit factory: Factory[A, C]): FactoryCompat[A, C] =
    new FactoryCompat[A, C] {
      override def newBuilder: mutable.Builder[A, C] = factory.newBuilder
    }
}
