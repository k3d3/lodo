package lodo

import java.util.UUID

import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._

import Helper._

object Dashboard {

  case class State(itemMap: ItemMap = items, selectedNotebook: Option[UUID] = None)

  class Backend(t: BackendScope[MainRouter.Router, State]) extends OnUnmount {
    def selectItem(item: Item) = {
      t.modState(_.copy(selectedNotebook = Some(item.id)))
    }

    def onClickComplete(item: Item) =
      applyOperation(itemMap => CompleteOp(item, itemMap.recursiveChildren(item.id)))

    def onNotebookClickAdd(item: Item) = {
      println("notebook add 2")
    }

    def applyOperation(op: Op): Unit = applyOperation(_ => op)

    def applyOperation(opBuild: ItemMap => Op) = {
      t.modState(s => {
        val op = opBuild(s.itemMap)
        s.copy(itemMap = s.itemMap(op))
      })
    }
  }

  val items: ItemMap = new ItemMap(Seq(
    Item(testId(0), None, 0, "Notebook0"),
    Item(testId(1), None, 5, "Notebook1"),
    Item(testId(9), None, 6, "Notebook2"),

    Item(UUID.randomUUID, Some(testId(0)), 1, "N1Page1"),
    Item(testId(2), Some(testId(0)), 2, "N1Page2"),

    Item(UUID.randomUUID, Some(testId(2)), 3, "N1P2List1"),
    Item(testId(3), Some(testId(2)), 11, "N1P2List2"),
    Item(UUID.randomUUID, Some(testId(3)), 7, "N1P2L2Item1"),

    Item(UUID.randomUUID, Some(testId(2)), 8, "N1P2List3"),
    Item(UUID.randomUUID, Some(testId(2)), 9, "N1P2List4"),
    Item(UUID.randomUUID, Some(testId(2)), 10, "N1P2List5")
  ))

  val dashboard = ReactComponentB[MainRouter.Router]("Dashboard")
    .initialStateP(router =>
      State(selectedNotebook = items.notebooks().headOption.map(_.id))
    )
    .backend(new Backend(_))
    .render((router, S, B) => {
      val appLinks = MainRouter.appLinks(router)
      <.div(
        Header(),
        Sidebar(Sidebar.Props(B, S.itemMap, S.selectedNotebook)),
        Contents(Contents.Props(B, S.itemMap, S.selectedNotebook))
      )
    }).build
}
