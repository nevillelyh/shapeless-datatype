import magnolia._
import scala.language.experimental.macros

trait Show[T] { def show(value: T): String }

object Show {

  type Typeclass[T] = Show[T]

  /** creates a new [[Show]] instance by labelling and joining (with `mkString`) the result of
   *  showing each parameter, and prefixing it with the class name */
  def combine[T](ctx: CaseClass[Typeclass, T]): Show[T] = { value =>
    if (ctx.isValueClass) {
      val param = ctx.parameters.head
      param.typeclass.show(param.dereference(value))
    } else {
      val paramStrings = ctx.parameters.map { param =>
        val attribStr = if(param.annotations.isEmpty) "" else {
          param.annotations.mkString("{", ", ", "}")
        }
        s"${param.label}$attribStr=${param.typeclass.show(param.dereference(value))}"
      }

      val anns = ctx.annotations.filterNot(_.isInstanceOf[scala.SerialVersionUID])
      val annotationStr = if (anns.isEmpty) "" else anns.mkString("{", ",", "}")

      join(ctx.typeName.short + annotationStr, paramStrings)
    }
  }

  /** choose which typeclass to use based on the subtype of the sealed trait */
  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Show[T] = (value: T) =>
    ctx.dispatch(value) { sub =>
      sub.typeclass.show(sub.cast(value))
    }

  /** bind the Magnolia macro to this derivation object */

  implicit def gen[T]: Show[T] = macro Magnolia.gen[T]

  implicit val string: Show[String] = (s: String) => s

  def join(typeName: String, params: Seq[String]): String =
    params.mkString(s"$typeName(", ",", ")")

  /** show typeclass for integers */
  implicit val int: Show[Int] = (s: Int) => s.toString

  /** show typeclass for sequences */
  implicit def seq[A](implicit A: Show[A]): Show[Seq[A]] =
    new Show[Seq[A]] {
      def show(as: Seq[A]): String = as.iterator.map(A.show).mkString("[", ",", "]")
    }
}
