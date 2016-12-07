package feed.utils

import java.time.format.DateTimeParseException

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.control.Exception._
object Pimp {

  def logger(_this: Object) = play.api.Logger("application." + _this.getClass.getName.stripSuffix("$"))
  def err(s: String) = throw new RuntimeException(s)

  implicit class RichOption[A](o: Option[A]){
    def orErr(msg: String): A =
      o.getOrElse(err(msg))
  }
  implicit class RichFuture[A](f: Future[A]){
    def await(atMost: Duration = Duration.Inf): A =
      Await.result(f, atMost)
    def tap(block: A => Unit)(implicit e: ExecutionContext): Future[A] =
      f.map { v =>
        block(v)
        v
      }
  }
  implicit class RichSeqFutures[A](fs: Seq[Future[A]]){
    def sequence(implicit e: ExecutionContext): Future[Seq[A]] =
      Future.sequence(fs)(Seq.canBuildFrom, e)
  }

  def timed[A](block: => Future[A])(implicit e: ExecutionContext): Future[(A, Duration)] = {
    val start = System.nanoTime()
    block.map { a =>
      a -> Duration.fromNanos(System.nanoTime() - start)
    }
  }

  def swallowing[T](exceptions: Class[_]*)(block: => T): Option[T] =
    catching(exceptions:_*).opt(block)

  def fuccess = Future.successful(())


}
