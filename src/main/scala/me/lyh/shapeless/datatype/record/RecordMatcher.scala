package me.lyh.shapeless.datatype.record

import shapeless._
import shapeless.labelled.FieldType

import scala.language.higherKinds

trait RecordMatcher[L <: HList] {
  def apply(l: L, r: L): Boolean
}

trait LowPriorityRecordMatcherBase {
  implicit def hconsRecordMatcherBase[K <: Symbol, V, T <: HList]
  (implicit rmT: Lazy[RecordMatcher[T]])
  : RecordMatcher[FieldType[K, V] :: T] = new RecordMatcher[FieldType[K, V] :: T] {
    override def apply(l: FieldType[K, V] :: T, r: FieldType[K, V] :: T): Boolean =
      l.head == r.head && rmT.value(l.tail, r.tail)
  }
}

trait LowPriorityRecordMatcher1 extends LowPriorityRecordMatcherBase {
  implicit def hconsRecordMatcher1[K <: Symbol, V, T <: HList]
  (implicit f: (V, V) => Boolean, rmT: Lazy[RecordMatcher[T]])
  : RecordMatcher[FieldType[K, V] :: T] = new RecordMatcher[FieldType[K, V] :: T] {
    override def apply(l: FieldType[K, V] :: T, r: FieldType[K, V] :: T): Boolean =
      f(l.head, r.head) && rmT.value(l.tail, r.tail)
  }
}

trait LowPriorityRecordMatcherOption1 extends LowPriorityRecordMatcher1 {
  implicit def hconsRecordMatcherOption1[K <: Symbol, V, T <: HList]
  (implicit f: (V, V) => Boolean, rmT: Lazy[RecordMatcher[T]])
  : RecordMatcher[FieldType[K, Option[V]] :: T] = new RecordMatcher[FieldType[K, Option[V]] :: T] {
    override def apply(l: FieldType[K, Option[V]] :: T, r: FieldType[K, Option[V]] :: T): Boolean = {
      val h = if (l.head.isDefined && r.head.isDefined)
        f(l.head.get, r.head.get)
      else
        l.head == r.head
      h && rmT.value(l.tail, r.tail)
    }
  }
}

trait LowPriorityRecordMatcherSeq1 extends LowPriorityRecordMatcherOption1 {
  implicit def hconsRecordMatcherSeq1[K <: Symbol, V, T <: HList, S[_] <: Seq[_]]
  (implicit f: (V, V) => Boolean, rmT: Lazy[RecordMatcher[T]])
  : RecordMatcher[FieldType[K, S[V]] :: T] = new RecordMatcher[FieldType[K, S[V]] :: T] {
    override def apply(l: FieldType[K, S[V]] :: T, r: FieldType[K, S[V]] :: T): Boolean = {
      val (ls, rs) = (l.head.asInstanceOf[Seq[V]], r.head.asInstanceOf[Seq[V]])
      val h = ls.size == rs.size && (ls zip rs).forall(f.tupled)
      h && rmT.value(l.tail, r.tail)
    }
  }
}

trait LowPriorityRecordMatcher0 extends LowPriorityRecordMatcherSeq1 {
  implicit def hconsRecordMatcher0[K <: Symbol, V, H <: HList, T <: HList]
  (implicit gen: LabelledGeneric.Aux[V, H],
   rmH: Lazy[RecordMatcher[H]], rmT: Lazy[RecordMatcher[T]])
  : RecordMatcher[FieldType[K, V] :: T] = new RecordMatcher[FieldType[K, V] :: T] {
    override def apply(l: FieldType[K, V] :: T, r: FieldType[K, V] :: T): Boolean =
      rmH.value(gen.to(l.head), gen.to(r.head)) && rmT.value(l.tail, r.tail)
  }
}

trait LowPriorityRecordMatcherOption0 extends LowPriorityRecordMatcher0 {
  implicit def hconsRecordMatcherOption0[K <: Symbol, V, H <: HList, T <: HList]
  (implicit gen: LabelledGeneric.Aux[V, H],
   rmH: Lazy[RecordMatcher[H]], rmT: Lazy[RecordMatcher[T]])
  : RecordMatcher[FieldType[K, Option[V]] :: T] = new RecordMatcher[FieldType[K, Option[V]] :: T] {
    override def apply(l: FieldType[K, Option[V]] :: T, r: FieldType[K, Option[V]] :: T): Boolean = {
      val h = if (l.head.isDefined && r.head.isDefined)
        rmH.value(gen.to(l.head.get), gen.to(r.head.get))
      else
        l.head == r.head
      h && rmT.value(l.tail, r.tail)
    }
  }
}

trait LowPriorityRecordMatcherSeq0 extends LowPriorityRecordMatcherOption0 {
  implicit def hconsRecordMatcherSeq0[K <: Symbol, V, H <: HList, T <: HList, S[_] <: Seq[_]]
  (implicit gen: LabelledGeneric.Aux[V, H],
   rmH: Lazy[RecordMatcher[H]], rmT: Lazy[RecordMatcher[T]])
  : RecordMatcher[FieldType[K, S[V]] :: T] = new RecordMatcher[FieldType[K, S[V]] :: T] {
    override def apply(l: FieldType[K, S[V]] :: T, r: FieldType[K, S[V]] :: T): Boolean = {
      val (ls, rs) = (l.head.asInstanceOf[Seq[V]], r.head.asInstanceOf[Seq[V]])
      val h = ls.size == rs.size && (ls zip rs).forall { case (x, y) => rmH.value(gen.to(x), gen.to(y)) }
      h && rmT.value(l.tail, r.tail)
    }
  }
}

object RecordMatcher extends LowPriorityRecordMatcherSeq0 {
  implicit val hnilRecordMatcher: RecordMatcher[HNil] = new RecordMatcher[HNil] {
    override def apply(l: HNil, r: HNil): Boolean = true
  }
}

class RecordMatcherType[A] extends Serializable {
  def apply[L <: HList](l: A, r: A)(implicit
                                    gen: LabelledGeneric.Aux[A, L],
                                    rm: RecordMatcher[L])
  : Boolean = rm(gen.to(l), gen.to(r))
}

object RecordMatcherType {
  def apply[A]: RecordMatcherType[A] = new RecordMatcherType[A]
}
