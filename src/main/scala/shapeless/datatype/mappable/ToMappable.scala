package shapeless.datatype.mappable

import shapeless._
import shapeless.labelled.FieldType

import scala.language.higherKinds

trait ToMappable[L <: HList, M] {
  def apply(l: L): M
}

trait LowPriorityToMappable1 {
  implicit def hconsToMappable1[K <: Symbol, V, T <: HList, M]
  (implicit wit: Witness.Aux[K], mt: MappableType[M, V], toT: Lazy[ToMappable[T, M]])
  : ToMappable[FieldType[K, V] :: T, M] = new ToMappable[FieldType[K, V] :: T, M] {
    override def apply(l: FieldType[K, V] :: T): M =
      mt.put(wit.value.name, l.head, toT.value(l.tail))
  }
}

trait LowPriorityToMappableOption1 extends LowPriorityToMappable1 {
  implicit def hconsToMappableOption1[K <: Symbol, V, T <: HList, M]
  (implicit wit: Witness.Aux[K], mt: MappableType[M, V], toT: Lazy[ToMappable[T, M]])
  : ToMappable[FieldType[K, Option[V]] :: T, M] = new ToMappable[FieldType[K, Option[V]] :: T, M] {
    override def apply(l: FieldType[K, Option[V]] :: T): M =
      mt.put(wit.value.name, l.head, toT.value(l.tail))
  }
}

trait LowPriorityToMappableSeq1 extends LowPriorityToMappableOption1 {
  implicit def hconsToMappableSeq1[K <: Symbol, V, T <: HList, M, S[_] <: Seq[_]]
  (implicit wit: Witness.Aux[K], mt: MappableType[M, V], toT: Lazy[ToMappable[T, M]])
  : ToMappable[FieldType[K, S[V]] :: T, M] = new ToMappable[FieldType[K, S[V]] :: T, M] {
    override def apply(l: FieldType[K, S[V]] :: T): M =
      mt.put(wit.value.name, l.head.asInstanceOf[Seq[V]], toT.value(l.tail))
  }
}

trait LowPriorityToMappable0 extends LowPriorityToMappableSeq1 {
  implicit def hconsToMappable0[K <: Symbol, V, H <: HList, T <: HList, M]
  (implicit wit: Witness.Aux[K], gen: LabelledGeneric.Aux[V, H], mbt: BaseMappableType[M],
   toH: Lazy[ToMappable[H, M]], toT: Lazy[ToMappable[T, M]])
  : ToMappable[FieldType[K, V] :: T, M] = new ToMappable[FieldType[K, V] :: T, M] {
    override def apply(l: FieldType[K, V] :: T): M =
      mbt.put(wit.value.name, toH.value(gen.to(l.head)), toT.value(l.tail))
  }
}

trait LowPriorityToMappableOption0 extends LowPriorityToMappable0 {
  implicit def hconsToMappableOption0[K <: Symbol, V, H <: HList, T <: HList, M]
  (implicit wit: Witness.Aux[K], gen: LabelledGeneric.Aux[V, H], mbt: BaseMappableType[M],
   toH: Lazy[ToMappable[H, M]], toT: Lazy[ToMappable[T, M]])
  : ToMappable[FieldType[K, Option[V]] :: T, M] = new ToMappable[FieldType[K, Option[V]] :: T, M] {
    override def apply(l: FieldType[K, Option[V]] :: T): M =
      mbt.put(wit.value.name, l.head.map(h => toH.value(gen.to(h))), toT.value(l.tail))
  }
}

trait LowPriorityToMappableSeq0 extends LowPriorityToMappableOption0 {
  implicit def hconsToMappableSeq0[K <: Symbol, V, H <: HList, T <: HList, M, S[_] <: Seq[_]]
  (implicit wit: Witness.Aux[K], gen: LabelledGeneric.Aux[V, H], mbt: BaseMappableType[M],
   toH: Lazy[ToMappable[H, M]], toT: Lazy[ToMappable[T, M]])
  : ToMappable[FieldType[K, S[V]] :: T, M] = new ToMappable[FieldType[K, S[V]] :: T, M] {
    override def apply(l: FieldType[K, S[V]] :: T): M =
      mbt.put(wit.value.name, l.head.asInstanceOf[Seq[V]].map(h => toH.value(gen.to(h))), toT.value(l.tail))
  }
}

object ToMappable extends LowPriorityToMappableSeq0 {
  implicit def hnilToMappable[M](implicit mbt: BaseMappableType[M])
  : ToMappable[HNil, M] = new ToMappable[HNil, M] {
    override def apply(l: HNil): M = mbt.base
  }
}
