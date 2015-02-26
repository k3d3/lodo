package lodo

import java.util.UUID

import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._

import Helper._

object Dashboard {
  case class State(items: Seq[Item] = items, selectedNotebook: Option[UUID] = None)

  class Backend(t: BackendScope[MainRouter.Router, State]) extends OnUnmount {
    def selectItem(item: Item) = {
      t.modState(s => s.copy(selectedNotebook = Some(item.id)))
    }
  }

  val items: Seq[Item] = Seq(
    Item(UUID.randomUUID, None, 0, "Test"),
    Item(UUID.randomUUID, None, 1, "Blah")
  )

  val dashboard = ReactComponentB[MainRouter.Router]("Dashboard")
    .initialState(State(selectedNotebook = items.headOption.map(_.id)))
    .backend(new Backend(_))
    .render((router, S, B) => {
      val appLinks = MainRouter.appLinks(router)
      <.div(
        Header(),
        Sidebar(Sidebar.Props(items, B.selectItem, S.selectedNotebook)),
        Contents(Contents.Props(S.selectedNotebook))
      )
    }).build
}
