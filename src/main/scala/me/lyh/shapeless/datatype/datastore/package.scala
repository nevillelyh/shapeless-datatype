package me.lyh.shapeless.datatype

import com.google.datastore.v1.Entity
import me.lyh.shapeless.datatype.mappable.{FromMappable, ToMappable}
import shapeless._

package object datastore extends DatastoreMappableType {
  type FromEntity[L <: HList] = FromMappable[L, Entity.Builder]
  type ToEntity[L <: HList] = ToMappable[L, Entity.Builder]
}
