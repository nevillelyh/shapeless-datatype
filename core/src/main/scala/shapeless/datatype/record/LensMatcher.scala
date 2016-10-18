package shapeless.datatype.record

import shapeless._
import shapeless.ops.hlist._

class LensMatcher[A, L <: HList](val root: OpticDefns.RootLens[A], val hs: L) extends Serializable {
  type ElemMatcher[Elem] = (Lens[A, Elem], (Elem, Elem) => Boolean)

  def on[Elem](lensFn: OpticDefns.RootLens[A] => Lens[A, Elem])
              (matchFn: (Elem, Elem) => Boolean)
  : LensMatcher[A, ElemMatcher[Elem] :: L] =
    new LensMatcher(root, (lensFn(root), matchFn) :: hs)

  private object matchFolder extends Poly2 {
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

/*
class LensMatcher[L <: HList](val hs: L) {
  private type ElemMatcher[A, Elem] = (Lens[A, Elem], (Elem, Elem) => Boolean)

  private object matchFolder extends Poly2 {
    implicit def default[A, Elem] =
      at[(A, A, Boolean), ElemMatcher[A, Elem]] { (z, m) =>
        val (l, r, b) = z
        val (lensA, cmp) = m
        (lensA.set(l)(lensA.get(r)), r, b && cmp(lensA.get(l), lensA.get(r)))
      }
  }

  def apply[A](l: A, r: A)(implicit lf: LeftFolder[L, (A, A, Boolean), matchFolder.type])
  : Boolean = {
    val (l1: A, r1: A, b1: Boolean) = hs.foldLeft((l, r, true))(matchFolder)
    l1 == r1 && b1
  }
}

object LensMatcher {
  def apply[L <: HList](hs: L): LensMatcher[L] = new LensMatcher[L](hs)
}

object LensMatcherApp {

  case class Record(id: String, name: String, value: Int)

  def cmpStr(x: String, y: String): Boolean = true

  def main(args: Array[String]): Unit = {
    val a = Record("abcde", "RecordA", 10)
    val b = Record("abcde", "RecordB", 10)
    val c = Record("ABCDE", "RecordA", 10)
    val d = Record("abcde", "RecordA", 20)
    val matchers = (lens[Record] >> 'name, cmpStr _) :: HNil

//    val m = LensMatcher(matchers)
//    println(m(a, b))
//    println(m(a, c))
//    println(m(a, d))

    val m1 = LensMatcher1[Record].on(_ >> 'name)(cmpStr)
    println(m1(a, b))
    println(m1(a, c))
    println(m1(a, d))
    println(m1(b, c))
    println(m1(b, d))
  }

}
*/