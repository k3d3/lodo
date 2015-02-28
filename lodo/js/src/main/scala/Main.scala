package lodo

import japgolly.scalajs.react.React
import japgolly.scalajs.react.extra.router.BaseUrl
import org.scalajs.dom

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport

@JSExport("Main")
object Main extends JSApp {
  @JSExport
  def main(): Unit = {
    dom.console.log("Hello")

    val baseUrl = BaseUrl(dom.window.location.href.takeWhile(_ != '#'))
    val router = MainRouter.router(baseUrl)

    React.render(router(), dom.document.body)
  }
}