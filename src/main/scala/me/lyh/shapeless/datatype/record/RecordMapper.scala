package me.lyh.shapeless.datatype.record

import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

trait RecordMapper[LI <: HList, LO <: HList] {
  def apply(l: LI): LO
}

trait LowPriorityRecordMapperBase {
  type MV[K, V, W, TI <: HList, TO <: HList] = RecordMapper[FieldType[K, V] :: TI, FieldType[K, W] :: TO]
  implicit def hconsRecordMapperBase[K <: Symbol, V, W, TI <: HList, TO <: HList]
  (implicit rmT: Lazy[RecordMapper[TI, TO]])
  : MV[K, V, V, TI, TO] = new MV[K, V, V, TI, TO] {
    override def apply(l: FieldType[K, V] :: TI): FieldType[K, V] :: TO =
      l.head :: rmT.value(l.tail)
  }
}

trait LowPriorityRecordMapper1 extends LowPriorityRecordMapperBase {
  implicit def hconsRecordMapper1[K <: Symbol, V, W, TI <: HList, TO <: HList]
  (implicit f: V => W, rmT: Lazy[RecordMapper[TI, TO]])
  : MV[K, V, W, TI, TO] = new MV[K, V, W, TI, TO] {
    override def apply(l: FieldType[K, V] :: TI): FieldType[K, W] :: TO =
      field[K](f(l.head)) :: rmT.value(l.tail)
  }
}

trait LowPriorityRecordMapperOption1 extends LowPriorityRecordMapper1 {
  implicit def hconsRecordMapperOption1[K <: Symbol, V, W, TI <: HList, TO <: HList]
  (implicit f: V => W, rmT: Lazy[RecordMapper[TI, TO]])
  : MV[K, Option[V], Option[W], TI, TO] = new MV[K, Option[V], Option[W], TI, TO] {
    override def apply(l: FieldType[K, Option[V]] :: TI): FieldType[K, Option[W]] :: TO =
      field[K](l.head.map(f)) :: rmT.value(l.tail)
  }
}

trait LowPriorityRecordMapperSeq1 extends LowPriorityRecordMapperOption1 {
  implicit def hconsRecordMapperSeq1[K <: Symbol, V, W, TI <: HList, TO <: HList, S[_] <: Seq[_]]
  (implicit f: V => W, rmT: Lazy[RecordMapper[TI, TO]],
   cbf: CanBuildFrom[_, W, S[W]])
  : MV[K, S[V], S[W], TI, TO] = new MV[K, S[V], S[W], TI, TO] {
    override def apply(l: FieldType[K, S[V]] :: TI): FieldType[K, S[W]] :: TO = {
      val b = cbf()
      b ++= l.head.asInstanceOf[Seq[V]].map(f)
      field[K](b.result()) :: rmT.value(l.tail)
    }
  }
}

trait LowPriorityRecordMapper0 extends LowPriorityRecordMapperSeq1 {
  implicit def hconsRecordMapper0[K <: Symbol, V, W, HV <: HList, HW <: HList, TI <: HList, TO <: HList]
  (implicit genV: LabelledGeneric.Aux[V, HV], genW: LabelledGeneric.Aux[W, HW],
   rmH: Lazy[RecordMapper[HV, HW]], rmT: Lazy[RecordMapper[TI, TO]])
  : MV[K, V, W, TI, TO] = new MV[K, V, W, TI, TO] {
    override def apply(l: FieldType[K, V] :: TI): FieldType[K, W] :: TO =
      field[K](genW.from(rmH.value(genV.to(l.head)))) :: rmT.value(l.tail)
  }
}

trait LowPriorityRecordMapperOption0 extends LowPriorityRecordMapper0 {
  implicit def hconsRecordMapperOption0[K <: Symbol, V, W, HV <: HList, HW <: HList, TI <: HList, TO <: HList]
  (implicit genV: LabelledGeneric.Aux[V, HV], genW: LabelledGeneric.Aux[W, HW],
   rmH: Lazy[RecordMapper[HV, HW]], rmT: Lazy[RecordMapper[TI, TO]])
  : MV[K, Option[V], Option[W], TI, TO] = new MV[K, Option[V], Option[W], TI, TO] {
    override def apply(l: FieldType[K, Option[V]] :: TI): FieldType[K, Option[W]] :: TO =
      field[K](l.head.map(v => genW.from(rmH.value(genV.to(v))))) :: rmT.value(l.tail)
  }
}

trait LowPriorityRecordMapperSeq0 extends LowPriorityRecordMapperOption0 {
  implicit def hconsRecordMapperSeq0[K <: Symbol, V, W, HV <: HList, HW <: HList, TI <: HList, TO <: HList, S[_] <: Seq[_]]
  (implicit genV: LabelledGeneric.Aux[V, HV], genW: LabelledGeneric.Aux[W, HW],
   rmH: Lazy[RecordMapper[HV, HW]], rmT: Lazy[RecordMapper[TI, TO]],
   cbf: CanBuildFrom[_, W, S[W]])
  : MV[K, S[V], S[W], TI, TO] = new MV[K, S[V], S[W], TI, TO] {
    override def apply(l: FieldType[K, S[V]] :: TI): FieldType[K, S[W]] :: TO = {
      val b = cbf()
      b ++=  l.head.asInstanceOf[Seq[V]].map(v => genW.from(rmH.value(genV.to(v))))
      field[K](b.result()) :: rmT.value(l.tail)
    }
  }
}

object RecordMapper extends LowPriorityRecordMapperSeq0 {
  implicit val hnilRecordMapper: RecordMapper[HNil, HNil] = new RecordMapper[HNil, HNil] {
    override def apply(l: HNil): HNil = l
  }
}

class RecordMapperType[A, B] extends Serializable {
  def to[LA <: HList, LB <: HList](a: A)(implicit
                                         genA: LabelledGeneric.Aux[A, LA],
                                         genB: LabelledGeneric.Aux[B, LB],
                                         rm: RecordMapper[LA, LB])
  : B = genB.from(rm(genA.to(a)))
  def from[LB <: HList, LA <: HList](b: B)(implicit
                                           genB: LabelledGeneric.Aux[B, LB],
                                           genA: LabelledGeneric.Aux[A, LA],
                                           rm: RecordMapper[LB, LA])
  : A = genA.from(rm(genB.to(b)))
}

object RecordMapperType {
  def apply[A, B]: RecordMapperType[A, B] = new RecordMapperType[A, B]()
}
