package shapeless.datatype.record

import shapeless._
import shapeless.labelled.FieldType

import scala.language.higherKinds

trait MatchRecord[L] {
  def apply(l: L, r: L): Boolean
}

trait CoproductMatchRecord {
  implicit def generic[T, R](implicit gen: LabelledGeneric.Aux[T, R], repr: MatchRecord[R]): MatchRecord[T] = new MatchRecord[T] {
    override def apply(l: T, r: T): Boolean = repr(gen.to(l), gen.to(r))
  }

  implicit val cnilMatchRecord: MatchRecord[CNil] = new MatchRecord[CNil] {
    override def apply(l: CNil, r: CNil) = true
  }

  implicit def cconsMatchRecord[K <: Symbol, H, T <: Coproduct](implicit W: Witness.Aux[K], H: MatchRecord[H], T: Lazy[MatchRecord[T]]): MatchRecord[FieldType[K, H] :+: T] = new MatchRecord[FieldType[K, H] :+: T] {
    override def apply(l: FieldType[K, H] :+: T, r: FieldType[K, H] :+: T): Boolean = (l, r) match {
      case (Inl(left), Inl(right)) => H(left, right)
      case (Inr(left), Inr(right)) => T.value(left, right)
      case _ => false
    }
  }
}

trait LowPriorityMatchRecordBase extends CoproductMatchRecord {
  implicit def hconsMatchRecordBase[K <: Symbol, V, T <: HList]
  (implicit mrT: Lazy[MatchRecord[T]])
  : MatchRecord[FieldType[K, V] :: T] = new MatchRecord[FieldType[K, V] :: T] {
    override def apply(l: FieldType[K, V] :: T, r: FieldType[K, V] :: T): Boolean =
      l.head == r.head && mrT.value(l.tail, r.tail)
  }
}

trait LowPriorityMatchRecord1 extends LowPriorityMatchRecordBase {
  implicit def hconsMatchRecord1[K <: Symbol, V, T <: HList]
  (implicit f: (V, V) => Boolean, mrT: Lazy[MatchRecord[T]])
  : MatchRecord[FieldType[K, V] :: T] = new MatchRecord[FieldType[K, V] :: T] {
    override def apply(l: FieldType[K, V] :: T, r: FieldType[K, V] :: T): Boolean =
      f(l.head, r.head) && mrT.value(l.tail, r.tail)
  }
}

trait LowPriorityMatchRecordOption1 extends LowPriorityMatchRecord1 {
  implicit def hconsMatchRecordOption1[K <: Symbol, V, T <: HList]
  (implicit f: (V, V) => Boolean, mrT: Lazy[MatchRecord[T]])
  : MatchRecord[FieldType[K, Option[V]] :: T] = new MatchRecord[FieldType[K, Option[V]] :: T] {
    override def apply(l: FieldType[K, Option[V]] :: T, r: FieldType[K, Option[V]] :: T): Boolean = {
      val h = if (l.head.isDefined && r.head.isDefined)
        f(l.head.get, r.head.get)
      else
        l.head == r.head
      h && mrT.value(l.tail, r.tail)
    }
  }
}

trait LowPriorityMatchRecordSeq1 extends LowPriorityMatchRecordOption1 {
  implicit def hconsMatchRecordSeq1[K <: Symbol, V, T <: HList, S[_] <: Seq[_]]
  (implicit f: (V, V) => Boolean, mrT: Lazy[MatchRecord[T]])
  : MatchRecord[FieldType[K, S[V]] :: T] = new MatchRecord[FieldType[K, S[V]] :: T] {
    override def apply(l: FieldType[K, S[V]] :: T, r: FieldType[K, S[V]] :: T): Boolean = {
      val (ls, rs) = (l.head.asInstanceOf[Seq[V]], r.head.asInstanceOf[Seq[V]])
      val h = ls.size == rs.size && (ls zip rs).forall(f.tupled)
      h && mrT.value(l.tail, r.tail)
    }
  }
}

trait LowPriorityMatchRecord0 extends LowPriorityMatchRecordSeq1 {
  implicit def hconsMatchRecord0[K <: Symbol, V, H <: HList, T <: HList]
  (implicit gen: LabelledGeneric.Aux[V, H],
   mrH: Lazy[MatchRecord[H]], mrT: Lazy[MatchRecord[T]])
  : MatchRecord[FieldType[K, V] :: T] = new MatchRecord[FieldType[K, V] :: T] {
    override def apply(l: FieldType[K, V] :: T, r: FieldType[K, V] :: T): Boolean =
      mrH.value(gen.to(l.head), gen.to(r.head)) && mrT.value(l.tail, r.tail)
  }
}

trait LowPriorityMatchRecordOption0 extends LowPriorityMatchRecord0 {
  implicit def hconsMatchRecordOption0[K <: Symbol, V, H <: HList, T <: HList]
  (implicit gen: LabelledGeneric.Aux[V, H],
   mrH: Lazy[MatchRecord[H]], mrT: Lazy[MatchRecord[T]])
  : MatchRecord[FieldType[K, Option[V]] :: T] = new MatchRecord[FieldType[K, Option[V]] :: T] {
    override def apply(l: FieldType[K, Option[V]] :: T, r: FieldType[K, Option[V]] :: T): Boolean = {
      val h = if (l.head.isDefined && r.head.isDefined)
        mrH.value(gen.to(l.head.get), gen.to(r.head.get))
      else
        l.head == r.head
      h && mrT.value(l.tail, r.tail)
    }
  }
}

trait LowPriorityMatchRecordSeq0 extends LowPriorityMatchRecordOption0 {
  implicit def hconsMatchRecordSeq0[K <: Symbol, V, H <: HList, T <: HList, S[_] <: Seq[_]]
  (implicit gen: LabelledGeneric.Aux[V, H],
   mrH: Lazy[MatchRecord[H]], mrT: Lazy[MatchRecord[T]])
  : MatchRecord[FieldType[K, S[V]] :: T] = new MatchRecord[FieldType[K, S[V]] :: T] {
    override def apply(l: FieldType[K, S[V]] :: T, r: FieldType[K, S[V]] :: T): Boolean = {
      val (ls, rs) = (l.head.asInstanceOf[Seq[V]], r.head.asInstanceOf[Seq[V]])
      val h = ls.size == rs.size && (ls zip rs).forall { case (x, y) => mrH.value(gen.to(x), gen.to(y)) }
      h && mrT.value(l.tail, r.tail)
    }
  }
}

object MatchRecord extends LowPriorityMatchRecordSeq0 {
  implicit val hnilMatchRecord: MatchRecord[HNil] = new MatchRecord[HNil] {
    override def apply(l: HNil, r: HNil): Boolean = true
  }
}

class RecordMatcher[A] extends Serializable {
  def apply(l: A, r: A)(implicit M: MatchRecord[A]) = M(l, r)
}

object RecordMatcher{
  def apply[A]: RecordMatcher[A] = new RecordMatcher[A]
}
