package lodo

import org.scalajs.dom
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._
import japgolly.scalajs.react.extra.router2.RouterCtl
import lodo.Main.{MainLoc, Loc}

object LoginManager {
  case class LoginData(username: String, authKey: String, encryptKey: String)

  case class State(loginData: Option[LoginData] = None)

  class Backend(t: BackendScope[RouterCtl[Loc], State]) {
    def onInit(): Unit = {
      t.modState(s => s.copy(
        loginData = for {
          username <- LocalStorage.getItem("lodoUsername").toOption
          authKey <- LocalStorage.getItem("lodoAuthKey").toOption
          encryptKey <- LocalStorage.getItem("lodoEncryptKey").toOption
        } yield LoginData(username, authKey, encryptKey))
      )
    }

    onInit()
  }

  val component = ReactComponentB[RouterCtl[Loc]]("Login")
    .initialState(State())
    .backend(new Backend(_))
    .render((router, S, B) => {
      <.div(
        Dashboard()
      )
    }).build
}
