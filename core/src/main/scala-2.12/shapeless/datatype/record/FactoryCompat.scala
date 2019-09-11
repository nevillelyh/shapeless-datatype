package shapeless.datatype.record

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

// From https://github.com/pureconfig/pureconfig
trait FactoryCompat[-A, +C] {
  def newBuilder: mutable.Builder[A, C]
}

object FactoryCompat {
  implicit def fromCanBuildFrom[A, C](implicit cbf: CanBuildFrom[_, A, C])
  : FactoryCompat[A, C] = new FactoryCompat[A, C] {
    override def newBuilder: mutable.Builder[A, C] = cbf()
  }
}
