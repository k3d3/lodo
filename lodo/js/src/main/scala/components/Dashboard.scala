package lodo

import java.util.UUID

import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._

import Helper._

object Dashboard {

  case class State(itemMap: ItemMap = items, selectedNotebook: Option[Item] = None)

  class Backend(t: BackendScope[MainRouter.Router, State]) extends OnUnmount {
    def selectItem(item: Item) = {
      t.modState(s => s.copy(selectedNotebook = Some(item)))
    }
  }

  val items: ItemMap = new ItemMap(Seq(
    Item(testId(0), None, 0, "Notebook0"),
    Item(testId(1), None, 5, "Notebook1"),
    Item(testId(2), None, 6, "Notebook2"),

    Item(UUID.randomUUID, Some(testId(1)), 1, "N1Page1"),
    Item(testId(3), Some(testId(1)), 2, "N1Page2"),

    Item(UUID.randomUUID, Some(testId(2)), 3, "N1P2List1"),
    Item(testId(4), Some(testId(2)), 11, "N1P2List2"),
    Item(UUID.randomUUID, Some(testId(3)), 7, "N1P2L2Item1"),

    Item(UUID.randomUUID, Some(testId(2)), 8, "N1P2List3"),
    Item(UUID.randomUUID, Some(testId(2)), 9, "N1P2List4"),
    Item(UUID.randomUUID, Some(testId(2)), 10, "N1P2List5")
  ))

  val dashboard = ReactComponentB[MainRouter.Router]("Dashboard")
    .initialStateP(router => State(selectedNotebook = items.notebooks.headOption))
    .backend(new Backend(_))
    .render((router, S, B) => {
      val appLinks = MainRouter.appLinks(router)
      <.div(
        Header(),
        Sidebar(Sidebar.Props(S.itemMap.notebooks, B.selectItem, S.selectedNotebook)),
        Contents(Contents.Props(S.itemMap, S.selectedNotebook))
      )
    }).build
}
