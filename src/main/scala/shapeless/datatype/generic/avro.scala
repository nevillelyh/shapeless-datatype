package shapeless.datatype.generic

import java.nio.ByteBuffer

import org.apache.avro.Schema
import org.apache.avro.specific.SpecificRecord
import shapeless._

import scala.collection.JavaConverters._
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@macrocompat.bundle
class AvroLabelledMacros(val c: whitebox.Context)
  extends SingletonTypeUtils with CaseClassMacros with AvroMacros {
  import c.universe._

  def mkDefaultSymbolicLabellingImpl[T](implicit tTag: WeakTypeTag[T]): Tree = {
    val tTpe = weakTypeOf[T]
    val labels: List[String] = avroSchemaOf(tTpe).getFields.asScala.map(_.name()).toList

    val labelTpes = labels.map(SingletonSymbolType(_))
    val labelValues = labels.map(mkSingletonSymbol)

    val labelsTpe = mkHListTpe(labelTpes)
    val labelsValue =
      labelValues.foldRight(q"_root_.shapeless.HNil": Tree) {
        case (elem, acc) => q"_root_.shapeless.::($elem, $acc)"
      }

    q"""
      new _root_.shapeless.DefaultSymbolicLabelling[$tTpe] {
        type Out = $labelsTpe
        def apply(): $labelsTpe = $labelsValue
      } : _root_.shapeless.DefaultSymbolicLabelling.Aux[$tTpe, $labelsTpe]
    """
  }
}

@macrocompat.bundle
trait AvroMacros {
  val c: whitebox.Context

  import c.universe._

  def avroSchemaOf(tpe: Type): Schema =
    Class.forName(tpe.typeSymbol.fullName)
      .getMethod("getClassSchema")
      .invoke(null)
      .asInstanceOf[Schema]

  def avroFieldsOf(tpe: Type): Seq[(TermName, Type)] = {
    val schema = avroSchemaOf(tpe)
    schema.getFields.asScala.toList.map { f =>
      (TermName(f.name()), typeOf[Int])
    }
  }

  def avroTypeOf(schema: Schema): Type = schema.getType match {
    case Schema.Type.INT => typeOf[Int]
    case Schema.Type.LONG => typeOf[Long]
    case Schema.Type.FLOAT => typeOf[Float]
    case Schema.Type.DOUBLE => typeOf[Double]
    case Schema.Type.BOOLEAN => typeOf[Boolean]
    case Schema.Type.STRING => typeOf[String]
    case Schema.Type.BYTES => typeOf[ByteBuffer]
//    case Schema.Type.ARRAY => tq"List[${avroTypeOf(schema.getElementType)}]"
  }

  def isAvro(tpe: Type): Boolean = tpe =:= typeOf[SpecificRecord]
}
