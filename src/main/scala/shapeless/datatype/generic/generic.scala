package shapeless.datatype.generic

import org.apache.avro.specific.SpecificRecord
import shapeless.GenericMacros

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

class RichGenericMacros(override val c: whitebox.Context) extends GenericMacros(c) with AvroMacros {
  import c.universe._
  import internal.constantType
  import Flag._

  def richMaterialize[T: WeakTypeTag, R: WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]

    if (tpe <:< typeOf[SpecificRecord])
      mkAvroGeneric(tpe)
    else
      materialize[T, R]
  }

  def mkAvroGeneric(tpe: Type): Tree = {
    q"""
       new Generic[$tpe] {
         type Repr = _root_.shapeless.HNil
         def to(p: $tpe): Repr = HNil
         def from(p: Repr): $tpe = null
       }.asInstanceOf[_root_.shapeless.Generic.Aux[$tpe, _root_.shapeless.HNil]]
     """
  }
}
