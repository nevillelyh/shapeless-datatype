package shapeless.datatype

import com.twitter.algebird._
import shapeless._

package object algebird {

  object SemigroupDerivedOrphans extends ProductTypeClassCompanion[Semigroup] {
    override val typeClass: ProductTypeClass[Semigroup] = new ProductTypeClass[Semigroup] {
      override def product[H, T <: HList](ch: Semigroup[H], ct: Semigroup[T]): Semigroup[H :: T] =
        Semigroup.from[H :: T] { (x, y) => ch.plus(x.head, y.head) :: ct.plus(x.tail, y.tail) }

      override def project[F, G](instance: => Semigroup[G], to: F => G, from: G => F): Semigroup[F] =
        Semigroup.from[F] { (x, y) => from(instance.plus(to(x), to(y))) }

      override def emptyProduct: Semigroup[HNil] = Semigroup.from[HNil]((_, _) => HNil)
    }
  }

  /*
  object MonoidDerivedOrphans extends ProductTypeClassCompanion[Monoid] {
    override val typeClass: ProductTypeClass[Monoid] = new ProductTypeClass[Monoid] {
      override def product[H, T <: HList](ch: Monoid[H], ct: Monoid[T]): Monoid[H :: T] =
        Monoid.from(ch.zero :: ct.zero) { (x, y) => ch.plus(x.head, y.head) :: ct.plus(x.tail, y.tail) }

      override def project[F, G](instance: => Monoid[G], to: F => G, from: G => F): Monoid[F] =
        Monoid.from(from(instance.zero)) { (x, y) => from(instance.plus(to(x), to(y)))}

      override def emptyProduct: Monoid[HNil] = Monoid.from[HNil](HNil)((_, _) => HNil)
    }
  }

  object GroupDerivedOrphans extends ProductTypeClassCompanion[Group] {
    override val typeClass: ProductTypeClass[Group] = new ProductTypeClass[Group] {
      override def product[H, T <: HList](ch: Group[H], ct: Group[T]): Group[H :: T] = ???

      override def project[F, G](instance: => Group[G], to: F => G, from: G => F): Group[F] = ???

      override def emptyProduct: Group[HNil] = ???
    }
  }

  object RingDerivedOrphans extends ProductTypeClassCompanion[Ring] {
    override val typeClass: ProductTypeClass[Ring] = new ProductTypeClass[Ring] {
      override def product[H, T <: HList](ch: Ring[H], ct: Ring[T]): Ring[H :: T] = ???

      override def emptyProduct: Ring[HNil] = ???

      override def project[F, G](instance: => Ring[G], to: F => G, from: G => F): Ring[F] = ???
    }
  }
  */

  implicit def deriveSemigroup[T]
  (implicit orphan: Orphan[Semigroup, SemigroupDerivedOrphans.type, T]): Semigroup[T] = orphan.instance

  /*
  implicit def deriveMonoid[T]
  (implicit orphan: Orphan[Monoid, MonoidDerivedOrphans.type, T]): Monoid[T] = orphan.instance

  implicit def deriveGroup[T]
  (implicit orphan: Orphan[Group, GroupDerivedOrphans.type, T]): Group[T] = orphan.instance

  implicit def deriveRing[T]
  (implicit orphan: Orphan[Ring, RingDerivedOrphans.type, T]): Ring[T] = orphan.instance
  */

}
