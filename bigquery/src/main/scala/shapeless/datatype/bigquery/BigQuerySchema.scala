package shapeless.datatype.bigquery

import com.google.api.services.bigquery.model.{TableFieldSchema, TableSchema}
import org.joda.time.{Instant, LocalDate, LocalDateTime, LocalTime}

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe._

object BigQuerySchema {

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

  private def rawType(tpe: Type): (String, Iterable[TableFieldSchema]) = tpe match {
    case t if t =:= typeOf[Boolean] => ("BOOLEAN", Nil)
    case t if t =:= typeOf[Int] => ("INTEGER", Nil)
    case t if t =:= typeOf[Long] => ("INTEGER", Nil)
    case t if t =:= typeOf[Float] => ("FLOAT", Nil)
    case t if t =:= typeOf[Double]  => ("FLOAT", Nil)
    case t if t =:= typeOf[String] => ("STRING", Nil)
    case t if t =:= typeOf[Array[Byte]] => ("BYTES", Nil)
    case t if t =:= typeOf[Instant] => ("TIMESTAMP", Nil)
    case t if t =:= typeOf[LocalDate] => ("DATE", Nil)
    case t if t =:= typeOf[LocalTime] => ("TIME", Nil)
    case t if t =:= typeOf[LocalDateTime] => ("DATETIME", Nil)
    case t if isCaseClass(t) => ("RECORD", toFields(t))
  }

  private def toField(s: Symbol): TableFieldSchema = {
    val name = s.name.toString
    val tpe = s.asMethod.returnType

    val (mode, valType) = tpe match {
      case t if t.erasure =:= typeOf[Option[_]].erasure => ("NULLABLE", typeArgs(t).head)
      case t if t.erasure <:< typeOf[Traversable[_]].erasure || (t.erasure <:< typeOf[Array[_]] && !(typeArgs(t).head =:= typeOf[Byte])) => ("REPEATED", typeArgs(t).head)
      case t => ("REQUIRED", t)
    }
    val (tpeParam, nestedParam) = customTypes.get(valType.toString) match {
      case Some(t) => (t, Nil)
      case None => rawType(valType)
    }
    val tfs = new TableFieldSchema().setMode(mode).setName(name).setType(tpeParam)
    if (nestedParam.nonEmpty) {
      tfs.setFields(nestedParam.toList.asJava)
    }
    tfs
  }

  // FIXME: Scala 2.11+ t.decls
  private def toFields(t: Type): Iterable[TableFieldSchema] = t.declarations.filter(isField).map(toField)

  private val customTypes = scala.collection.mutable.Map[String, String]()
  // FIXME: Scala 2.11+ TypeTag[_]
  private val cachedSchemas = scala.collection.concurrent.TrieMap.empty[Type, TableSchema]

  private[bigquery] def register(tpe: Type, typeName: String): Unit =
    customTypes += tpe.toString -> typeName

  // FIXME: Scala 2.11+ implicitly[TypeTag[T]], remove synchronized
  def apply[T: TypeTag]: TableSchema = {
    val t = BigQuerySchema.synchronized(implicitly[TypeTag[T]].tpe)
    cachedSchemas.getOrElseUpdate(t, BigQuerySchema.synchronized(new TableSchema().setFields(toFields(t).toList.asJava)))
  }

}
