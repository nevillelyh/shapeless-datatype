package shapeless.datatype.record

import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

trait MapRecord[LI <: HList, LO <: HList] {
  def apply(l: LI): LO
}

trait LowPriorityMapRecordBase {
  type MV[K, V, W, TI <: HList, TO <: HList] = MapRecord[FieldType[K, V] :: TI, FieldType[K, W] :: TO]
  implicit def hconsMapRecordBase[K <: Symbol, V, W, TI <: HList, TO <: HList]
  (implicit mrT: Lazy[MapRecord[TI, TO]])
  : MV[K, V, V, TI, TO] = new MV[K, V, V, TI, TO] {
    override def apply(l: FieldType[K, V] :: TI): FieldType[K, V] :: TO =
      l.head :: mrT.value(l.tail)
  }
}

trait LowPriorityMapRecord1 extends LowPriorityMapRecordBase {
  implicit def hconsMapRecord1[K <: Symbol, V, W, TI <: HList, TO <: HList]
  (implicit f: V => W, mrT: Lazy[MapRecord[TI, TO]])
  : MV[K, V, W, TI, TO] = new MV[K, V, W, TI, TO] {
    override def apply(l: FieldType[K, V] :: TI): FieldType[K, W] :: TO =
      field[K](f(l.head)) :: mrT.value(l.tail)
  }
}

trait LowPriorityMapRecordOption5 extends LowPriorityMapRecord1 {
  implicit def hconsMapRecordOption5[K <: Symbol, V, W, TI <: HList, TO <: HList]
  (implicit f: V => W, mrT: Lazy[MapRecord[TI, TO]], extractor: UnsafeOptionExtractor[V])
  : MV[K, Option[V], W, TI, TO] = new MV[K, Option[V], W, TI, TO] {
    override def apply(l: FieldType[K, Option[V]] :: TI): FieldType[K, W] :: TO =
      field[K](f(extractor.extract(l.head))) :: mrT.value(l.tail)
  }
}

trait LowPriorityMapRecordOption4 extends LowPriorityMapRecordOption5 {
  implicit def hconsMapRecordOption4[K <: Symbol, V, W, TI <: HList, TO <: HList]
  (implicit f: V => W, mrT: Lazy[MapRecord[TI, TO]])
  : MV[K, V, Option[W], TI, TO] = new MV[K, V, Option[W], TI, TO] {
    override def apply(l: FieldType[K, V] :: TI): FieldType[K, Option[W]] :: TO =
      field[K](Some(f(l.head))) :: mrT.value(l.tail)
  }
}

trait LowPriorityMapRecordOption3 extends LowPriorityMapRecordOption4 {
  implicit def hconsMapRecordOption3[K <: Symbol, V, W, TI <: HList, TO <: HList]
  (implicit f: V => W, mrT: Lazy[MapRecord[TI, TO]])
  : MV[K, Option[V], Option[W], TI, TO] = new MV[K, Option[V], Option[W], TI, TO] {
    override def apply(l: FieldType[K, Option[V]] :: TI): FieldType[K, Option[W]] :: TO =
      field[K](l.head.map(f)) :: mrT.value(l.tail)
  }
}

trait LowPriorityMapRecordIterable1 extends LowPriorityMapRecordOption3 {
  implicit def hconsMapRecordIterable1[K <: Symbol, V, W, TI <: HList, TO <: HList, S[_] <: Iterable[_]]
  (implicit f: V => W, mrT: Lazy[MapRecord[TI, TO]],
   cbf: CanBuildFrom[_, W, S[W]])
  : MV[K, S[V], S[W], TI, TO] = new MV[K, S[V], S[W], TI, TO] {
    override def apply(l: FieldType[K, S[V]] :: TI): FieldType[K, S[W]] :: TO = {
      val b = cbf()
      b ++= l.head.asInstanceOf[Iterable[V]].map(f)
      field[K](b.result()) :: mrT.value(l.tail)
    }
  }
}

trait LowPriorityMapRecord0 extends LowPriorityMapRecordIterable1 {
  implicit def hconsMapRecord0[K <: Symbol, V, W, HV <: HList, HW <: HList, TI <: HList, TO <: HList]
  (implicit genV: LabelledGeneric.Aux[V, HV], genW: LabelledGeneric.Aux[W, HW],
   mrH: Lazy[MapRecord[HV, HW]], mrT: Lazy[MapRecord[TI, TO]])
  : MV[K, V, W, TI, TO] = new MV[K, V, W, TI, TO] {
    override def apply(l: FieldType[K, V] :: TI): FieldType[K, W] :: TO =
      field[K](genW.from(mrH.value(genV.to(l.head)))) :: mrT.value(l.tail)
  }
}

trait LowPriorityMapRecordOption2 extends LowPriorityMapRecord0 {
  implicit def hconsMapRecordOption2[K <: Symbol, V, W, HV <: HList, HW <: HList, TI <: HList, TO <: HList]
  (implicit genV: LabelledGeneric.Aux[V, HV], genW: LabelledGeneric.Aux[W, HW],
   mrH: Lazy[MapRecord[HV, HW]], mrT: Lazy[MapRecord[TI, TO]], extractor: UnsafeOptionExtractor[V])
  : MV[K, Option[V], W, TI, TO] = new MV[K, Option[V], W, TI, TO] {
    override def apply(l: FieldType[K, Option[V]] :: TI): FieldType[K, W] :: TO =
      field[K](genW.from(mrH.value(genV.to(extractor.extract(l.head))))) :: mrT.value(l.tail)
  }
}

trait LowPriorityMapRecordOption1 extends LowPriorityMapRecordOption2 {
  implicit def hconsMapRecordOption1[K <: Symbol, V, W, HV <: HList, HW <: HList, TI <: HList, TO <: HList]
  (implicit genV: LabelledGeneric.Aux[V, HV], genW: LabelledGeneric.Aux[W, HW],
   mrH: Lazy[MapRecord[HV, HW]], mrT: Lazy[MapRecord[TI, TO]])
  : MV[K, V, Option[W], TI, TO] = new MV[K, V, Option[W], TI, TO] {
    override def apply(l: FieldType[K, V] :: TI): FieldType[K, Option[W]] :: TO =
      field[K](Some(genW.from(mrH.value(genV.to(l.head))))) :: mrT.value(l.tail)
  }
}

trait LowPriorityMapRecordOption0 extends LowPriorityMapRecordOption1 {
  implicit def hconsMapRecordOption0[K <: Symbol, V, W, HV <: HList, HW <: HList, TI <: HList, TO <: HList]
  (implicit genV: LabelledGeneric.Aux[V, HV], genW: LabelledGeneric.Aux[W, HW],
   mrH: Lazy[MapRecord[HV, HW]], mrT: Lazy[MapRecord[TI, TO]])
  : MV[K, Option[V], Option[W], TI, TO] = new MV[K, Option[V], Option[W], TI, TO] {
    override def apply(l: FieldType[K, Option[V]] :: TI): FieldType[K, Option[W]] :: TO =
      field[K](l.head.map(v => genW.from(mrH.value(genV.to(v))))) :: mrT.value(l.tail)
  }
}

trait LowPriorityMapRecordIterable0 extends LowPriorityMapRecordOption0 {
  implicit def hconsMapRecordIterable0[K <: Symbol, V, W, HV <: HList, HW <: HList, TI <: HList, TO <: HList, S[_] <: Iterable[_]]
  (implicit genV: LabelledGeneric.Aux[V, HV], genW: LabelledGeneric.Aux[W, HW],
   mrH: Lazy[MapRecord[HV, HW]], mrT: Lazy[MapRecord[TI, TO]],
   cbf: CanBuildFrom[_, W, S[W]])
  : MV[K, S[V], S[W], TI, TO] = new MV[K, S[V], S[W], TI, TO] {
    override def apply(l: FieldType[K, S[V]] :: TI): FieldType[K, S[W]] :: TO = {
      val b = cbf()
      b ++=  l.head.asInstanceOf[Iterable[V]].map(v => genW.from(mrH.value(genV.to(v))))
      field[K](b.result()) :: mrT.value(l.tail)
    }
  }
}

object MapRecord extends LowPriorityMapRecordIterable0 {
  implicit val hnilMapRecord: MapRecord[HNil, HNil] = new MapRecord[HNil, HNil] {
    override def apply(l: HNil): HNil = l
  }
}

class RecordMapper[A, B] extends Serializable {
  def to[LA <: HList, LB <: HList](a: A)(implicit
                                         genA: LabelledGeneric.Aux[A, LA],
                                         genB: LabelledGeneric.Aux[B, LB],
                                         mr: MapRecord[LA, LB])
  : B = genB.from(mr(genA.to(a)))
  def from[LB <: HList, LA <: HList](b: B)(implicit
                                           genB: LabelledGeneric.Aux[B, LB],
                                           genA: LabelledGeneric.Aux[A, LA],
                                           mr: MapRecord[LB, LA])
  : A = genA.from(mr(genB.to(b)))
}

object RecordMapper {
  def apply[A, B]: RecordMapper[A, B] = new RecordMapper[A, B]()
}

class UnsafeOptionExtractor[T] {
  def extract(opt: Option[T]): T = opt.get
}
object UnsafeOptionExtractorImplicits {
  implicit def apply[T]: UnsafeOptionExtractor[T] = new UnsafeOptionExtractor[T]
}