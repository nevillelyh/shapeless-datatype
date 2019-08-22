package shapeless.datatype.mappable

import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.collection.mutable
import scala.language.higherKinds

trait FromMappable[L <: HList, M] extends Serializable {
  def apply(m: M): Option[L]
}

trait SerializableCanBuildFrom {
  // Workaround for CanBuildFrom not serializable
  trait CanBuild[T, M] extends Serializable {
    def apply(): mutable.Builder[T, M]
  }

  private def newCB[T, M](f: () => mutable.Builder[T, M]) = new CanBuild[T, M] {
    override def apply(): mutable.Builder[T, M] = f()
  }

  // Collection types in _root_.scala.*
  implicit def arrayCB[T: scala.reflect.ClassTag] = newCB(() => Array.newBuilder[T])
  implicit def traversableCB[T] = newCB(() => Traversable.newBuilder[T])
  implicit def iterableCB[T] = newCB(() => Iterable.newBuilder[T])
  implicit def seqCB[T] = newCB(() => Seq.newBuilder[T])
  implicit def indexedSeqCB[T] = newCB(() => IndexedSeq.newBuilder[T])
  implicit def listCB[T] = newCB(() => List.newBuilder[T])
  implicit def vectorCB[T] = newCB(() => Vector.newBuilder[T])
  implicit def setCB[T] = newCB(() => Set.newBuilder[T])
}

trait LowPriorityFromMappable1 extends SerializableCanBuildFrom {
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

trait LowPriorityFromMappableIterable1 extends LowPriorityFromMappableOption1 {
  implicit def hconsFromMappableIterable1[K <: Symbol, V, T <: HList, M, S[_] <: Iterable[_]]
  (implicit wit: Witness.Aux[K], mt: MappableType[M, V], fromT: Lazy[FromMappable[T, M]],
   cbf: CanBuild[V, S[V]], toIterable: S[V] => Iterable[V])
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

trait LowPriorityFromMappable0 extends LowPriorityFromMappableIterable1 {
  implicit def hconsFromMappable0[K <: Symbol, V, H <: HList, T <: HList, M: CanNest]
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
  implicit def hconsFromMappableOption0[K <: Symbol, V, H <: HList, T <: HList, M: CanNest]
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

trait LowPriorityFromMappableIterable0 extends LowPriorityFromMappableOption0 {
  implicit def hconsFromMappableIterable0[K <: Symbol, V, H <: HList, T <: HList, M: CanNest, S[_] <: Iterable[_]]
  (implicit wit: Witness.Aux[K], gen: LabelledGeneric.Aux[V, H], bmt: BaseMappableType[M],
   fromH: Lazy[FromMappable[H, M]], fromT: Lazy[FromMappable[T, M]],
   cbf: CanBuild[V, S[V]], toIterable: S[V] => Iterable[V])
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

object FromMappable extends LowPriorityFromMappableIterable0 {
  implicit def hnilFromMappable[M]: FromMappable[HNil, M] = new FromMappable[HNil, M] {
    override def apply(m: M): Option[HNil] = Some(HNil)
  }
}
