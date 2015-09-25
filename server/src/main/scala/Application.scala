/*
Lodo is a layered to-do list (Outliner)
Copyright (C) 2015 Keith Morrow.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License v3 as
published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package controllers

import java.nio.ByteBuffer

import akka.actor.ActorSystem
import boopickle.Default._
import play.api.mvc._

import scala.concurrent.Future
import scala.util.{Failure, Success, Properties}
import scala.concurrent.ExecutionContext.Implicits.global

object Router extends autowire.Server[ByteBuffer, Pickler, Pickler] {
  def read[R: Pickler](p: ByteBuffer) = Unpickle[R].fromBytes(p)
  def write[R: Pickler](r: R) = Pickle.intoBytes(r)
}

object Application extends Controller {
  //val apiService = new ApiService()
  
  def index = Action {
    Ok("This is the index")
  }

  def lodo = Action {
    Ok(views.html.lodo())
  }

  def logging = Action(parse.anyContent) {
    implicit request =>
      request.body.asJson.foreach { msg =>
        println(s"CLIENT - $msg")
      }
      Ok("")
  }
}

/*object Server extends SimpleRoutingApp {
  def main(args: Array[String]): Unit = {


    implicit val system = ActorSystem("Main")
    implicit val context = system.dispatcher

    val port = Properties.envOrElse("PORT", "5000").toInt

    val devMode = System.getProperty("DEVMODE", "false").toBoolean

    val apiService = new ApiService(system)

    startServer("0.0.0.0", port = port) {
      get {
        pathSingleSlash {
          if (devMode)
            getFromResource("web/index.html")
          else
            getFromResource("web/index-full.html")
        } ~
        (if (devMode)
          getFromResourceDirectory("web")
        else
          compressResponse() (getFromResourceDirectory("web")))
      } ~
      post {
        path("api" / Segments) { s =>
          extract(_.request.entity.asString) { e =>
            onComplete(Future {
              Router.route[LodoApi](apiService)(
                autowire.Core.Request(s, upickle.read[Map[String, String]](e))
              )
            }) {
              case Success(value) => complete(value)
              case Failure(f) => complete(s"failure! $f")
            }
          }
        }
      }
    }
  }
}*/
