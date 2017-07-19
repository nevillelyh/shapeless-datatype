package shapeless.datatype

import scala.language.higherKinds

package object diff extends DeriveDiff {
  implicit def numericDiff[T: Numeric]: Diff[T] = Diff.from[T](Delta.Kind.Numeric) { (x, y) =>
    val num = implicitly[Numeric[T]]
    import num.mkNumericOps
    (y - x).toDouble()
  }

  implicit val stringDiff: Diff[String] = Diff.from[String](Delta.Kind.String)(Levenshtein.distance)

  implicit def setDiff[T]: Diff[Set[T]] = Diff.from[Set[T]](Delta.Kind.Set)(Jaccard.distance)

  implicit def vectorDiff[T: Numeric, M[_]](implicit ev: M[T] => Seq[T]): Diff[M[T]] =
    Diff.from[M[T]](Delta.Kind.Vector)(Cosine.distance)

  object Cosine {
    def distance[T: Numeric, M[_]](xs: M[T], ys: M[T])(implicit ev: M[T] => Seq[T]): Double =
      1.0 - sim(xs, ys)

    private def sim[T: Numeric, M[_]](xs: M[T], ys: M[T])(implicit ev: M[T] => Seq[T]): Double = {
      val num = implicitly[Numeric[T]]
      import num.mkNumericOps
      var (dp, xss, yss) = (0.0, 0.0, 0.0)
      var i = 0
      val (xi, yi) = (ev(xs).iterator, ev(ys).iterator)
      while (xi.hasNext && yi.hasNext) {
        val (x, y) = (xi.next().toDouble(), yi.next().toDouble())
        dp += x * y
        xss += x * x
        yss += y * y
        i += 1
      }
      require(xi.hasNext == yi.hasNext, "Vectors have different dimensions")
      dp / math.sqrt(xss * yss)
    }
  }

  object Jaccard {
    def distance[T](xs: Set[T], ys: Set[T]): Double = 1.0 - index(xs, ys)
    def index[T](xs: Set[T], ys: Set[T]): Double = {
      val nom = (xs intersect ys).size
      val denom = xs.size + ys.size - nom
      if (denom == 0) 1.0 else nom.toDouble / denom
    }
  }

  object Levenshtein {
    def distance(x: String, y: String): Int = {
      val dist = Array.tabulate(y.length + 1, x.length + 1) { (j, i) =>
        if (j == 0) i else if (i == 0) j else 0
      }
      for (j <- 1 to y.length; i <- 1 to x.length) {
        dist(j)(i) = if (y(j - 1) == x(i - 1)) {
          dist(j - 1)(i - 1)
        } else {
          minimum(dist(j - 1)(i) + 1, dist(j)(i - 1) + 1, dist(j - 1)(i - 1) + 1)
        }
      }
      dist(y.length)(x.length)
    }

    private def minimum(i1: Int, i2: Int, i3: Int): Int = math.min(math.min(i1, i2), i3)
  }
}
