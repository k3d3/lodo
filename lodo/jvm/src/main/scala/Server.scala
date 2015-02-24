package lodo

import akka.actor.ActorSystem
import spray.routing.SimpleRoutingApp

import scala.util.Properties

object Router extends autowire.Server[String, upickle.Reader, upickle.Writer] {
  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}

object Server extends SimpleRoutingApp {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("Main")
    implicit val context = system.dispatcher

    val port = Properties.envOrElse("PORT", "5000").toInt

    val apiService = new ApiService

    startServer("0.0.0.0", port = port) {
      get {
        pathSingleSlash {
          getFromResource("web/index.html")
        } ~
        getFromResourceDirectory("web")
      } ~
      post {
        path("api" / Segments) { s =>
          extract(_.request.entity.asString) { e =>
            complete {
              Router.route[LodoApi](apiService)(
                autowire.Core.Request(s, upickle.read[Map[String, String]](e))
              )
            }
          }
        }
      }
    }
  }
}