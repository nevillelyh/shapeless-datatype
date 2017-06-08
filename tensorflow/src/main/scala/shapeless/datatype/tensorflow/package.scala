package shapeless.datatype

import org.tensorflow.example.Features
import shapeless._
import shapeless.datatype.mappable.{FromMappable, ToMappable}

package object tensorflow extends TensorFlowMappableType {
  type FromFeatures[L <: HList] = FromMappable[L, Features.Builder]
  type ToFeatures[L <: HList] = ToMappable[L, Features.Builder]
}
