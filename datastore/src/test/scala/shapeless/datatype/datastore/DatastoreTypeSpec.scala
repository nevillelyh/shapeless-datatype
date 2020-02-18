package shapeless.datatype.datastore

import java.net.URI

import com.google.datastore.v1.Entity
import com.google.datastore.v1.client.DatastoreHelper._
import org.scalacheck.Prop.{all, forAll}
import org.scalacheck.ScalacheckShapeless._
import org.scalacheck._
import shapeless._
import shapeless.datatype.record._

object DatastoreTypeSpec extends Properties("DatastoreType") {
  import shapeless.datatype.test.Records._
  import shapeless.datatype.test.SerializableUtils._

  implicit def compareByteArrays(x: Array[Byte], y: Array[Byte]) = java.util.Arrays.equals(x, y)
  implicit def compareIntArrays(x: Array[Int], y: Array[Int]) = java.util.Arrays.equals(x, y)

  def roundTrip[A, L <: HList](m: A)(
    implicit
    gen: LabelledGeneric.Aux[A, L],
    fromL: FromEntity[L],
    toL: ToEntity[L],
    mr: MatchRecord[L]
  ): Prop = {
    val t = ensureSerializable(DatastoreType[A])
    val f1: SerializableFunction[A, Entity] =
      new SerializableFunction[A, Entity] {
        override def apply(m: A): Entity = t.toEntity(m)
      }
    val f2: SerializableFunction[Entity, Option[A]] =
      new SerializableFunction[Entity, Option[A]] {
        override def apply(m: Entity): Option[A] = t.fromEntity(m)
      }
    val f3: SerializableFunction[A, Entity.Builder] =
      new SerializableFunction[A, Entity.Builder] {
        override def apply(m: A): Entity.Builder = t.toEntityBuilder(m)
      }
    val f4: SerializableFunction[Entity.Builder, Option[A]] =
      new SerializableFunction[Entity.Builder, Option[A]] {
        override def apply(m: Entity.Builder): Option[A] = t.fromEntityBuilder(m)
      }
    val toFn1 = ensureSerializable(f1)
    val fromFn1 = ensureSerializable(f2)
    val toFn2 = ensureSerializable(f3)
    val fromFn2 = ensureSerializable(f4)
    val copy1 = fromFn1(toFn1(m))
    val copy2 = fromFn2(toFn2(m))
    val rm = RecordMatcher[A]
    all(copy1.exists(rm(_, m)), copy2.exists(rm(_, m)))
  }

  property("required") = forAll { m: Required => roundTrip(m) }
  property("optional") = forAll { m: Optional => roundTrip(m) }
  property("repeated") = forAll { m: Repeated => roundTrip(m) }
  property("mixed") = forAll { m: Mixed => roundTrip(m) }
  property("nested") = forAll { m: Nested => roundTrip(m) }
  property("seqs") = forAll { m: Seqs => roundTrip(m) }

  implicit val uriDatastoreType =
    DatastoreType.at[URI](v => URI.create(v.getStringValue), u => makeValue(u.toString).build())
  property("custom") = forAll { m: Custom => roundTrip(m) }
}
