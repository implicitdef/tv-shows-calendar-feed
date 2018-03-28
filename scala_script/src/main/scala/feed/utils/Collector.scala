package feed.utils

import scala.collection._

class Collector(var throwables: Seq[Throwable] = Nil) {

  def push(errorMessage: String): Unit =
    push(new RuntimeException(errorMessage))
  def push(t: Throwable): Unit =
    throwables = throwables :+ t


}
