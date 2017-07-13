package shapeless.datatype.bigquery

import com.google.api.services.bigquery.model.TableRow
import shapeless._

import scala.reflect.runtime.universe._

class BigQueryType[A] extends Serializable {
  def fromTableRow[L <: HList](m: TableRow)
                              (implicit gen: LabelledGeneric.Aux[A, L], fromL: FromTableRow[L])
  : Option[A] = fromL(m.asInstanceOf[BigQueryMap]).map(gen.from)
  def toTableRow[L <: HList](a: A)
                            (implicit gen: LabelledGeneric.Aux[A, L], toL: ToTableRow[L])
  : TableRow = {
    val tr = new TableRow()
    tr.putAll(toL(gen.to(a)))
    tr
  }
}

object BigQueryType {
  def apply[A]: BigQueryType[A] = new BigQueryType[A]

  def at[V: TypeTag](typeName: String)(fromFn: Any => V, toFn: V => Any): BaseBigQueryMappableType[V] = {
    BigQuerySchema.register(implicitly[TypeTag[V]].tpe, typeName)
    new BaseBigQueryMappableType[V] {
      override def from(value: Any): V = fromFn(value)
      override def to(value: V): Any = toFn(value)
    }
  }
}
