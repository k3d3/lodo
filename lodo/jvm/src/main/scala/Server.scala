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

package lodo

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import spray.httpx.encoding.Gzip
import spray.routing.SimpleRoutingApp
import upickle._

import scala.concurrent.Future
import scala.util.{Failure, Success, Properties}

import Helper._

object Router extends autowire.Server[String, upickle.Reader, upickle.Writer] {
  def read[Result: upickle.Reader](p: String) = upickle.read[Result](p)
  def write[Result: upickle.Writer](r: Result) = upickle.write(r)
}

object Config {
  val c = ConfigFactory.load().getConfig("lodo")

  val productionMode = c.getBoolean("productionMode")
}

object Server extends SimpleRoutingApp {
  def main(args: Array[String]): Unit = {


    implicit val system = ActorSystem("Main")
    implicit val context = system.dispatcher

    val port = Properties.envOrElse("PORT", "5000").toInt

    val apiService = new ApiService(system)

    startServer("0.0.0.0", port = port) {
      get {
        pathSingleSlash {
          if (Config.productionMode)
            getFromResource("web/index-full.html")
          else
            getFromResource("web/index.html")
        } ~
        (if (Config.productionMode)
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
}