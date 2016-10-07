package me.lyh.shapeless.datatype.bigquery

import shapeless._

class BigQueryType[A] extends Serializable {
  def fromTableRow[L <: HList](m: TableRow)
                              (implicit gen: LabelledGeneric.Aux[A, L], fromL: FromTableRow[L])
  : Option[A] = fromL(m).map(gen.from)
  def toTableRow[L <: HList](a: A)
                            (implicit gen: LabelledGeneric.Aux[A, L], toL: ToTableRow[L])
  : TableRow = toL(gen.to(a))
}

object BigQueryType {
  def apply[A]: BigQueryType[A] = new BigQueryType[A]
}
