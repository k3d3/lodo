package lodo

import java.util.UUID

import akka.actor.ActorSystem
import spray.routing.SimpleRoutingApp

import scala.util.Properties

import lodo.Helper._

object Router extends autowire.Server[String, upickle.Reader, upickle.Writer] {
  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}

object Server extends SimpleRoutingApp {
  val items: ItemMap = new ItemMap(Seq(
    Item(testId(0), None, time(), "Notebook0"),
    Item(testId(1), None, time()+1, "Notebook1"),
    Item(testId(9), None, time()+2, "Notebook2"),

    Item(UUID.randomUUID, Some(testId(0)), time()+3, "N1Page1"),
    Item(testId(2), Some(testId(0)), time()+4, "N1Page2"),

    Item(UUID.randomUUID, Some(testId(2)), time()+5, "N1P2List1"),
    Item(testId(3), Some(testId(2)), time()+6, "N1P2List2"),
    Item(UUID.randomUUID, Some(testId(3)), time()+7, "N1P2L2Item1"),

    Item(UUID.randomUUID, Some(testId(2)), time()+8, "N1P2List3"),
    Item(UUID.randomUUID, Some(testId(2)), time()+9, "N1P2List4"),
    Item(UUID.randomUUID, Some(testId(2)), time()+10, "N1P2List5")
  ))

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