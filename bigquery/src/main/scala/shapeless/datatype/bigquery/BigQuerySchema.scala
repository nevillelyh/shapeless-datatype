package shapeless.datatype.bigquery

import com.google.api.services.bigquery.model.{TableFieldSchema, TableSchema}
import com.google.protobuf.ByteString
import org.joda.time.{Instant, LocalDate, LocalDateTime, LocalTime}

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe._

object BigQuerySchema {

  private def isField(s: Symbol): Boolean =
    s.isPublic && s.isMethod && !s.isSynthetic && !s.isConstructor

  private def isCaseClass(tpe: Type): Boolean =
    !tpe.toString.startsWith("scala.") &&
      List(typeOf[Product], typeOf[Serializable], typeOf[Equals])
        .forall(b => tpe.baseClasses.contains(b.typeSymbol))

  private def rawType(tpe: Type): (String, Iterable[TableFieldSchema]) = tpe match {
    case t if t =:= typeOf[Boolean] => ("BOOLEAN", Nil)
    case t if t =:= typeOf[Int] => ("INTEGER", Nil)
    case t if t =:= typeOf[Long] => ("INTEGER", Nil)
    case t if t =:= typeOf[Float] => ("FLOAT", Nil)
    case t if t =:= typeOf[Double]  => ("FLOAT", Nil)
    case t if t =:= typeOf[String] => ("STRING", Nil)
    case t if t =:= typeOf[ByteString] => ("BYTES", Nil)
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
      case t if t.erasure =:= typeOf[Option[_]].erasure => ("NULLABLE", t.typeArgs.head)
      case t if t.erasure <:< typeOf[Traversable[_]].erasure || (t.erasure <:< typeOf[Array[_]] && !(t.typeArgs.head =:= typeOf[Byte])) => ("REPEATED", t.typeArgs.head)
      case t => ("REQUIRED", t)
    }
    val (tpeParam, nestedParam) = rawType(valType)
    val tfs = new TableFieldSchema().setMode(mode).setName(name).setType(tpeParam)
    if (nestedParam.nonEmpty) {
      tfs.setFields(nestedParam.toList.asJava)
    }
    tfs
  }

  private def toFields(t: Type): Iterable[TableFieldSchema] = t.decls.filter(isField).map(toField)

  private val m = new java.util.concurrent.ConcurrentHashMap[TypeTag[_], TableSchema]()

  def apply[T: TypeTag]: TableSchema = m.computeIfAbsent(
    implicitly[TypeTag[T]],
    new java.util.function.Function[TypeTag[_], TableSchema] {
      override def apply(t: TypeTag[_]): TableSchema =
        new TableSchema().setFields(toFields(t.tpe).toList.asJava)
    })
  def main(args: Array[String]): Unit = {
  }

}
