object Test {
  case class A(i: Int, s: String, b: B)
  case class B(s: String)
  def main(args: Array[String]): Unit = {
    import Show._
    println(gen[A].show(A(1, "A", B("b"))))
  }
}
