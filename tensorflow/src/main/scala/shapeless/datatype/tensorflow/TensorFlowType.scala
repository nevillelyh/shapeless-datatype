package shapeless.datatype.tensorflow

import org.tensorflow.example.Example
import shapeless._

class TensorFlowType[A] extends Serializable {
  def fromExampleBuilder[L <: HList](m: Example.Builder)
                                    (implicit
                                     gen: LabelledGeneric.Aux[A, L],
                                     fromL: FromFeatures[L]): Option[A] =
    fromL(m.getFeaturesBuilder).map(gen.from)

  def fromExample[L <: HList](m: Example)
                             (implicit
                              gen: LabelledGeneric.Aux[A, L],
                              fromL: FromFeatures[L]): Option[A] =
    fromL(m.getFeatures.toBuilder).map(gen.from)

  def toExampleBuilder[L <: HList](a: A)
                                  (implicit
                                  gen: LabelledGeneric.Aux[A, L],
                                  toL: ToFeatures[L]): Example.Builder =
    Example.newBuilder().setFeatures(toL(gen.to(a)))

  def toExample[L <: HList](a: A)
                           (implicit
                            gen: LabelledGeneric.Aux[A, L],
                            toL: ToFeatures[L]): Example =
    Example.newBuilder().setFeatures(toL(gen.to(a))).build()
}

object TensorFlowType {
  def apply[A]: TensorFlowType[A] = new TensorFlowType[A]
}
