package shapeless.datatype.record

import shapeless._
import shapeless.ops.hlist._

class LensMatcher[A, L <: HList](val root: OpticDefns.RootLens[A], val hs: L) extends Serializable {
  type ElemMatcher[Elem] = (Lens[A, Elem], (Elem, Elem) => Boolean)

  def on[Elem](
    lensFn: OpticDefns.RootLens[A] => Lens[A, Elem]
  )(matchFn: (Elem, Elem) => Boolean): LensMatcher[A, ElemMatcher[Elem] :: L] =
    new LensMatcher(root, (lensFn(root), matchFn) :: hs)

  object matchFolder extends Poly2 {
    implicit def default[Elem] = at[(A, A, Boolean), ElemMatcher[Elem]] { (z, m) =>
      val (l, r, b) = z
      val (lensA, cmp) = m
      (lensA.set(l)(lensA.get(r)), r, b && cmp(lensA.get(l), lensA.get(r)))
    }
  }

  def apply(l: A, r: A)(implicit lf: LeftFolder[L, (A, A, Boolean), matchFolder.type]): Boolean = {
    val (l1, r1, b1: Boolean) = hs.foldLeft((l, r, true))(matchFolder)
    l1 == r1 && b1
  }
}

object LensMatcher {
  def apply[A]: LensMatcher[A, HNil] = new LensMatcher(lens[A], HNil)
}
