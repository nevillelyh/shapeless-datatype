package shapeless.datatype.record

import shapeless._
import shapeless.ops.hlist._

class LensMatcher[A, L <: HList](val root: OpticDefns.RootLens[A], val hs: L) extends Serializable {
  type ElemMatcher[Elem] = (Lens[A, Elem], (Elem, Elem) => Boolean)

  def on[Elem](lensFn: OpticDefns.RootLens[A] => Lens[A, Elem])
              (matchFn: (Elem, Elem) => Boolean)
  : LensMatcher[A, ElemMatcher[Elem] :: L] =
    new LensMatcher(root, (lensFn(root), matchFn) :: hs)

  object matchFolder extends Poly2 {
    implicit def default[Elem] = at[(A, A, Boolean), ElemMatcher[Elem]] { (z, m) =>
      val (l, r, b) = z
      val (lensA, cmp) = m
      (lensA.set(l)(lensA.get(r)), r, b && cmp(lensA.get(l), lensA.get(r)))
    }
  }

  def apply(l: A, r: A)(implicit lf: LeftFolder[L, (A, A, Boolean), matchFolder.type]): Boolean = {
    val (l1: A, r1: A, b1: Boolean) = hs.foldLeft((l, r, true))(matchFolder)
    l1 == r1 && b1
  }

  def wrap(value: A)(implicit lf: LeftFolder[L, (A, A, Boolean), matchFolder.type]): Wrapped =
    new Wrapped(value, lf)

  class Wrapped private[record] (val value: A,
                                 private val lf: LeftFolder[L, (A, A, Boolean), matchFolder.type])
    extends Serializable {
    override def toString: String = value.toString
    override def hashCode(): Int = value.hashCode()
    override def equals(obj: Any): Boolean =
      if (obj.getClass == classOf[Wrapped]) {
        val that = obj.asInstanceOf[Wrapped]
        val (l1, r1, b1: Boolean) = hs.foldLeft((value, that.value, true))(matchFolder)(lf)
        l1 == r1 && b1
      } else if (obj.getClass == value.getClass) {
        val that = obj.asInstanceOf[A]
        val (l1, r1, b1: Boolean) = hs.foldLeft((value, that, true))(matchFolder)(lf)
        l1 == r1 && b1
      } else {
        false
      }
  }
}

object LensMatcher {
  def apply[A]: LensMatcher[A, HNil] = new LensMatcher(lens[A], HNil)
}
