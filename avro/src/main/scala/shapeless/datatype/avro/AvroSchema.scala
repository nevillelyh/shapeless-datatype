package shapeless.datatype.avro

import org.apache.avro.Schema.Field
import org.apache.avro.{JsonProperties, Schema}

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe._

object AvroSchema {

  // FIXME: Scala 2.11+ && !s.isConstructor
  private def isField(s: Symbol): Boolean =
    s.isPublic && s.isMethod && !s.isSynthetic && !s.asMethod.isConstructor

  private def isCaseClass(tpe: Type): Boolean =
    !tpe.toString.startsWith("scala.") &&
      List(typeOf[Product], typeOf[Serializable], typeOf[Equals])
        .forall(b => tpe.baseClasses.contains(b.typeSymbol))

  // FIXME: Scala 2.11+ t.typeArgs.head
  private def typeArgs(t: Type): List[Type] = {
    val TypeRef(_, _, typeArgs) = t
    typeArgs
  }

  private def toSchema(tpe: Type): (Schema, Any) = tpe match {
    case t if t =:= typeOf[Boolean] => (Schema.create(Schema.Type.BOOLEAN), null)
    case t if t =:= typeOf[Int] => (Schema.create(Schema.Type.INT), null)
    case t if t =:= typeOf[Long] => (Schema.create(Schema.Type.LONG), null)
    case t if t =:= typeOf[Float] => (Schema.create(Schema.Type.FLOAT), null)
    case t if t =:= typeOf[Double] => (Schema.create(Schema.Type.DOUBLE), null)
    case t if t =:= typeOf[String] => (Schema.create(Schema.Type.STRING), null)
    case t if t =:= typeOf[Array[Byte]] => (Schema.create(Schema.Type.BYTES), null)

    case t if t.erasure =:= typeOf[Option[_]].erasure =>
      val s = toSchema(typeArgs(t).head)._1
      (Schema.createUnion(Schema.create(Schema.Type.NULL), s), JsonProperties.NULL_VALUE)
    case t if t.erasure <:< typeOf[Traversable[_]].erasure || t.erasure <:< typeOf[Array[_]] =>
      val s = toSchema(typeArgs(t).head)._1
      (Schema.createArray(s), java.util.Collections.emptyList())

    case t if isCaseClass(t) =>
      // FIXME: Scala 2.11+ t.decls
      val fields: List[Field] = t.declarations.filter(isField).map(toField)(scala.collection.breakOut)
      val name = t.typeSymbol.name.toString
      val pkg = t.typeSymbol.owner.fullName
      (Schema.createRecord(name, null, pkg, false, fields.asJava), null)

    case t if customTypes.contains(t.toString) => (Schema.create(customTypes(t.toString)), null)
  }

  private def toField(s: Symbol): Field = {
    val name = s.name.toString
    val tpe = s.asMethod.returnType
    val (schema, default) = toSchema(tpe)
    new Field(name, schema, null, default)
  }

  private val customTypes = scala.collection.mutable.Map[String, Schema.Type]()
  // FIXME: Scala 2.11+ TypeTag[_]
  private val cachedSchemas = scala.collection.concurrent.TrieMap.empty[Type, Schema]

  private[avro] def register(tpe: Type, schemaType: Schema.Type): Unit =
    customTypes += tpe.toString -> schemaType

  // FIXME: Scala 2.11+ implicitly[TypeTag[T]], remove synchronized
  def apply[T: TypeTag]: Schema = {
    val t = AvroSchema.synchronized(implicitly[TypeTag[T]].tpe)
    cachedSchemas.getOrElseUpdate(t, AvroSchema.synchronized(toSchema(t)._1))
  }

}
