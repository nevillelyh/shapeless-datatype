package shapeless.datatype.diff

sealed trait Delta
object Delta {
  sealed trait Kind
  object Kind {
    case object Numeric extends Kind
    case object String extends Kind
    case object Set extends Kind
    case object Vector extends Kind
  }

  case object Zero extends Delta
  case class Field(kind: Kind, value: Double) extends Delta
  case class Product(deltas: Map[String, Delta]) extends Delta
}

trait Diff[T] extends Serializable {
  def delta(x: T, y: T): Delta
}

object Diff {
  def const[T](const: Delta): Diff[T] = new Diff[T] {
    override def delta(x: T, y: T): Delta = const
  }

  def from[T](kind: Delta.Kind)(f: (T, T) => Double): Diff[T] = new Diff[T] {
    override def delta(x: T, y: T): Delta = f(x, y) match {
      case 0.0 => Delta.Zero
      case d => Delta.Field(kind, d)
    }
  }

  def apply[T: Diff](x: T, y: T): Delta = implicitly[Diff[T]].delta(x, y)
}
