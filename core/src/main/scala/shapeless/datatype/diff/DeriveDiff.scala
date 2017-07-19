package shapeless.datatype.diff

import shapeless._

import scala.language.higherKinds

trait DeriveDiff {
  object DiffDerivedOrphans extends LabelledProductTypeClassCompanion[Diff] {
    override val typeClass: LabelledProductTypeClass[Diff] = new LabelledProductTypeClass[Diff] {
      override def product[H, T <: HList](name: String, ch: Diff[H], ct: Diff[T]): Diff[H :: T] =
        new Diff[H :: T] {
          override def delta(x: H :: T, y: H :: T): Delta =
            (ch.delta(x.head, y.head), ct.delta(x.tail, y.tail)) match {
              case (Delta.Zero, t) => t
              case (h, Delta.Zero) => Delta.Product(name -> h :: Nil)
              case (h, Delta.Product(xs)) => Delta.Product(name -> h :: xs)
              case _ => throw new IllegalStateException("This should never happen")
            }
        }

      override def project[F, G](instance: => Diff[G], to: F => G, from: G => F): Diff[F] =
        new Diff[F] {
          override def delta(x: F, y: F): Delta = instance.delta(to(x), to(y))
        }

      override def emptyProduct: Diff[HNil] = Diff.const(Delta.Zero)
    }
  }

  implicit def deriveDiff[T]
  (implicit orphan: Orphan[Diff, DiffDerivedOrphans.type, T]): Diff[T] = orphan.instance
}
