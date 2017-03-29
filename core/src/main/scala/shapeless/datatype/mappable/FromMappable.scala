package shapeless.datatype.mappable

import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

trait FromMappable[L, M] {
  def apply(m: M): Option[L]
}

trait CoproductFromMappable {
  implicit def generic[T, R, M](implicit gen: LabelledGeneric.Aux[T, R], repr: FromMappable[R, M], mbt: BaseMappableType[M]): FromMappable[T, M] = new FromMappable[T, M] {
    override def apply(m: M): Option[T] = repr(m).map(x => gen.from(x))
  }


  implicit def cnilFromMappable[M]: FromMappable[CNil, M] = new FromMappable[CNil, M] {
    override def apply(m: M): Option[CNil] = None
  }

  implicit def cconsFromMappable[M, K <: Symbol, V, T <: Coproduct](implicit key: Witness.Aux[K], mt: BaseMappableType[M], sv: FromMappable[V, M], st: Lazy[FromMappable[T, M]]): FromMappable[FieldType[K, V] :+: T, M] = new FromMappable[FieldType[K, V] :+: T, M] {
    override def apply(m: M): Option[:+:[FieldType[K, V], T]] = {
      if(mt.get(m, s"__${key.value.name}").nonEmpty) sv(m).map(v => Inl(field[K](v)))
      else st.value(m).map(Inr.apply)
    }

  }
}

trait LowPriorityFromMappable1 extends CoproductFromMappable {
  implicit def hconsFromMappable1[K <: Symbol, V, T <: HList, M]
  (implicit wit: Witness.Aux[K], mt: MappableType[M, V], fromT: Lazy[FromMappable[T, M]])
  : FromMappable[FieldType[K, V] :: T, M] = new FromMappable[FieldType[K, V] :: T, M] {
    override def apply(m: M): Option[FieldType[K, V] :: T] = for {
      h <- mt.get(m, wit.value.name)
      t <- fromT.value(m)
    } yield field[K](h) :: t
  }
}

trait LowPriorityFromMappableOption1 extends LowPriorityFromMappable1 {
  implicit def hconsFromMappableOption1[K <: Symbol, V, T <: HList, M]
  (implicit wit: Witness.Aux[K], mt: MappableType[M, V], fromT: Lazy[FromMappable[T, M]])
  : FromMappable[FieldType[K, Option[V]] :: T, M] = new FromMappable[FieldType[K, Option[V]] :: T, M] {
    override def apply(m: M): Option[FieldType[K, Option[V]] :: T] = for {
      t <- fromT.value(m)
    } yield field[K](mt.get(m, wit.value.name)) :: t
  }
}

trait LowPriorityFromMappableSeq1 extends LowPriorityFromMappableOption1 {
  implicit def hconsFromMappableSeq1[K <: Symbol, V, T <: HList, M, S[_]]
  (implicit wit: Witness.Aux[K], mt: MappableType[M, V], fromT: Lazy[FromMappable[T, M]],
   cbf: CanBuildFrom[_, V, S[V]], toSeq: S[V] => Seq[V])
  : FromMappable[FieldType[K, S[V]] :: T, M] = new FromMappable[FieldType[K, S[V]] :: T, M] {
    override def apply(m: M): Option[FieldType[K, S[V]] :: T] = for {
      t <- fromT.value(m)
    } yield {
      val b = cbf()
      b ++= mt.getAll(m, wit.value.name)
      field[K](b.result()) :: t
    }
  }
}

trait LowPriorityFromMappable0 extends LowPriorityFromMappableSeq1 {
  implicit def hconsFromMappable0[K <: Symbol, V, H <: HList, T <: HList, M]
  (implicit wit: Witness.Aux[K], gen: LabelledGeneric.Aux[V, H], bmt: BaseMappableType[M],
   fromH: Lazy[FromMappable[H, M]], fromT: Lazy[FromMappable[T, M]])
  : FromMappable[FieldType[K, V] :: T, M] = new FromMappable[FieldType[K, V] :: T, M] {
    override def apply(m: M): Option[FieldType[K, V] :: T] = for {
      n <- bmt.get(m, wit.value.name)
      h <- fromH.value(n)
      t <- fromT.value(m)
    } yield field[K](gen.from(h)) :: t
  }
}

trait LowPriorityFromMappableOption0 extends LowPriorityFromMappable0 {
  implicit def hconsFromMappableOption0[K <: Symbol, V, H <: HList, T <: HList, M]
  (implicit wit: Witness.Aux[K], gen: LabelledGeneric.Aux[V, H], bmt: BaseMappableType[M],
   fromH: Lazy[FromMappable[H, M]], fromT: Lazy[FromMappable[T, M]])
  : FromMappable[FieldType[K, Option[V]] :: T, M] = new FromMappable[FieldType[K, Option[V]] :: T, M] {
    override def apply(m: M): Option[FieldType[K, Option[V]] :: T] = for {
      t <- fromT.value(m)
    } yield {
      val o = for {
        n <- bmt.get(m, wit.value.name)
        h <- fromH.value(n)
      } yield gen.from(h)
      field[K](o) :: t
    }
  }
}

trait LowPriorityFromMappableSeq0 extends LowPriorityFromMappableOption0 {
  implicit def hconsFromMappableSeq0[K <: Symbol, V, H <: HList, T <: HList, M, S[_]]
  (implicit wit: Witness.Aux[K], gen: LabelledGeneric.Aux[V, H], bmt: BaseMappableType[M],
   fromH: Lazy[FromMappable[H, M]], fromT: Lazy[FromMappable[T, M]],
   cbf: CanBuildFrom[_, V, S[V]], toSeq: S[V] => Seq[V])
  : FromMappable[FieldType[K, S[V]] :: T, M] = new FromMappable[FieldType[K, S[V]] :: T, M] {
    override def apply(m: M): Option[FieldType[K, S[V]] :: T] = for {
      t <- fromT.value(m)
    } yield {
      val b = cbf()
      b ++= (for {
        n <- bmt.getAll(m, wit.value.name)
        h <- fromH.value(n)
      } yield gen.from(h))
      field[K](b.result()) :: t
    }
  }
}

object FromMappable extends LowPriorityFromMappableSeq0 {
  implicit def hnilFromMappable[M](implicit mbt: BaseMappableType[M]): FromMappable[HNil, M] = new FromMappable[HNil, M] {
    override def apply(m: M): Option[HNil] = Some(HNil)
  }
}
