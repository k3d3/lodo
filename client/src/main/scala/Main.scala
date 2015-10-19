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

import japgolly.scalajs.react.React
import japgolly.scalajs.react.extra.router2._
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.typedarray._
import scala.scalajs.js.annotation.JSExport


@JSExport("Main")
object Main extends js.JSApp {
  sealed trait Loc
  case object MainLoc extends Loc

  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._
    (staticRoute(root, MainLoc) ~> renderR(ctl => LoginManager.component(ctl)))
      .notFound(redirectToPage(MainLoc)(Redirect.Replace))
  }

  @JSExport
  def main(): Unit = {

    val router = Router(BaseUrl(dom.window.location.href.takeWhile(_ != '#')), routerConfig)

    React.render(router(), dom.document.getElementById("root"))

    /*val receiver = LibSodium.cryptoBoxKeypair()

    val testString = LibSodium.Utf8Codec.encode("hello")

    val cipherText = LibSodium.cryptoBoxSeal(testString, receiver.publicKey)

    dom.console.log(cipherText)

    val plainText = LibSodium.cryptoBoxSealOpen(cipherText, receiver.publicKey, receiver.privateKey)
    
    dom.console.log(LibSodium.Utf8Codec.decode(plainText))*/
  }
}
