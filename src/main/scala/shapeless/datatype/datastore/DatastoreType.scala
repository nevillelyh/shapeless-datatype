package shapeless.datatype.datastore

import com.google.datastore.v1.Entity
import shapeless._

class DatastoreType[A] extends Serializable {
  def fromEntity[L <: HList](m: Entity)
                            (implicit gen: LabelledGeneric.Aux[A, L], fromL: FromEntity[L])
  : Option[A] = fromL(m.toBuilder).map(gen.from)
  def toEntity[L <: HList](a: A)
                          (implicit gen: LabelledGeneric.Aux[A, L], toL: ToEntity[L])
  : Entity = toL(gen.to(a)).build()
}

object DatastoreType {
  def apply[A]: DatastoreType[A] = new DatastoreType[A]
}
