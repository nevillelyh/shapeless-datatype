package shapeless.datatype

import com.google.datastore.v1.Entity
import shapeless._
import shapeless.datatype.mappable.{CanNest, FromMappable, ToMappable}

package object datastore extends DatastoreMappableType {
  type FromEntity[L <: HList] = FromMappable[L, Entity.Builder]
  type ToEntity[L <: HList] = ToMappable[L, Entity.Builder]

  implicit object DatastoreCanNest extends CanNest[Entity.Builder]
}
