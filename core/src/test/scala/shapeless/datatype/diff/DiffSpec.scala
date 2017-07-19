package shapeless.datatype.diff

import org.scalacheck.Prop.{BooleanOperators, all, forAll}
import org.scalacheck.Shapeless._
import org.scalacheck._

object DiffSpec extends Properties("Diff") {

  import shapeless.datatype.test.SerializableUtils._

  type P[T] = (T, T)

  // Int has less chance of overflow
  implicit val arbIntList = Arbitrary(Gen.choose(10, 10)
    .flatMap(n => Gen.listOfN(n, Arbitrary.arbitrary[Int])))
  implicit val arbIntArray = Arbitrary(arbIntList.arbitrary.map(_.toArray))

  def test[T: Diff](p: P[T], kind: Delta.Kind,
                    lowerBound: Double = Double.MinValue,
                    upperBound: Double = Double.MaxValue): Prop = all(
    "equal1" |: Diff(p._1, p._1) == Delta.Zero,
    "equal2" |: Diff(p._2, p._2) == Delta.Zero,
    "diff" |: { Diff(p._1, p._2) match {
      case Delta.Zero => p._1 == p._2
      case Delta.Field(k, v) => k == kind && v != 0.0 && lowerBound <= v && v <= upperBound
      case _ => throw new IllegalStateException("This should never happen")
    } },
    "serializable" |: ensureSerializable(implicitly[Diff[T]]) != null
  )

  property("int") = forAll { p: P[Int] => test(p, Delta.Kind.Numeric) }
  property("long") = forAll { p: P[Long] => test(p, Delta.Kind.Numeric) }
  property("float") = forAll { p: P[Float] => test(p, Delta.Kind.Numeric) }
  property("double") = forAll { p: P[Double] => test(p, Delta.Kind.Numeric) }
  property("string") = forAll { p: P[String] =>
    test(p, Delta.Kind.String, 0.0, math.max(p._1.length, p._2.length))
  }
  property("set") = forAll { p: P[Set[Int]] => test(p, Delta.Kind.Set, 0.0, 1.0) }
  property("list vector") = forAll { p: P[List[Int]] => test(p, Delta.Kind.Vector, 0.0, 2.0) }

  case class A(a: Int, b: Long, c: Float, d: Double)
  case class B(a: A, b: Int)

  def diffA(x: A, y: A): Delta = Delta.Product(List(
    ("a", Diff(x.a, y.a)),
    ("b", Diff(x.b, y.b)),
    ("c", Diff(x.c, y.c)),
    ("d", Diff(x.d, y.d))
  ).filter(_._2 != Delta.Zero).toMap)

  def diffB(x: B, y: B): Delta = Delta.Product(List(
    ("a", diffA(x.a, y.a)),
    ("b", Diff(x.b, y.b))
  ).filter(_._2 != Delta.Zero).toMap)

  property("flat") = forAll { p: P[A] =>
    all(
      "equal1" |: Diff(p._1, p._1) == Delta.Zero,
      "equal2" |: Diff(p._2, p._2) == Delta.Zero,
      "diff" |: Diff(p._1, p._2) == diffA(p._1, p._2),
      "serializable" |: ensureSerializable(implicitly[Diff[A]]) != null)
  }
  property("nested") = forAll { p: P[B] =>
    all(
      "equal1" |: Diff(p._1, p._1) == Delta.Zero,
      "equal2" |: Diff(p._2, p._2) == Delta.Zero,
      "diff" |: Diff(p._1, p._2) == diffB(p._1, p._2),
      "serializable" |: ensureSerializable(implicitly[Diff[B]]) != null)
  }

  case class C(a: Int, b: Long, c: List[String])

  def diffC(x: C, y: C): Delta = Delta.Product(List(
    ("a", Diff(x.a, y.a)),
    ("b", Diff(x.b, y.b)),
    ("c", if (x.c.size == y.c.size) Delta.Zero else Delta.Field(Delta.Kind.Vector, y.c.size - x.c.size))
  ).filter(_._2 != Delta.Zero).toMap)

  implicit val listStringDiff =
    Diff.from[List[String]](Delta.Kind.Vector)((x, y) => y.size - x.size)
  property("custom") = forAll { p: P[C] =>
    all(
      "equal1" |: Diff(p._1, p._1) == Delta.Zero,
      "equal2" |: Diff(p._2, p._2) == Delta.Zero,
      "diff" |: Diff(p._1, p._2) == diffC(p._1, p._2),
      "serializable" |: ensureSerializable(implicitly[Diff[C]]) != null)
  }

}
