package gen

import akka.actor.{Actor, Props, ActorSystem}
import akka.contrib.throttle.Throttler.{SetTarget, Rate}
import akka.contrib.throttle.TimerBasedThrottler
import akka.util.Timeout
import gen.HttpThrottler.HttpActor
import gen.utils.Pimp._
import play.api.libs.ws.{WSResponse, WSRequest}
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.pattern.ask

object HttpThrottler {

  object HttpActor {
    def props = Props[HttpActor]
  }
  class HttpActor extends Actor {
    val log = logger(this)
    import context.dispatcher
    def receive =   {
      case req: WSRequest =>
        log.debug(s">>> ${req.url}")
        val _sender = sender
        req.execute().foreach { wsResponse =>
          log.debug(s"<<< ${wsResponse.status}")
          _sender ! wsResponse
        }
    }
  }
}

class HttpThrottler(rate: Rate = Rate(10, 1.second)) {

  val actorSystem = ActorSystem()
  val httpActor = actorSystem.actorOf(HttpActor.props, "httpActor")
  val throttledHttpActor = actorSystem.actorOf(Props(classOf[TimerBasedThrottler], Rate(10, 1.second)), "throttledHttpActor")
  throttledHttpActor ! SetTarget(Some(httpActor))
  implicit val timeout = new Timeout(10.minutes)

  def shutdown() = {
    actorSystem.terminate()
  }

  def call(wSRequest: WSRequest): Future[WSResponse] =
    throttledHttpActor.?(wSRequest).mapTo[WSResponse]

}
