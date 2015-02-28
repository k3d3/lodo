package lodo

import java.util.UUID

import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._

import Helper._

object Dashboard {

  case class State(itemMap: ItemMap = items,
                   selectedNotebook: Option[UUID] = None,
                   isAdding: Boolean = false)

  class Backend(t: BackendScope[MainRouter.Router, State]) extends OnUnmount {
    def selectItem(item: Item) = {
      println("select")
      t.modState(s =>
        s.copy(selectedNotebook = Some(item.id),
          isAdding = if (s.selectedNotebook == Some(item.id)) s.isAdding else false))
    }

    def onClickComplete(item: Item) =
      applyOperation(itemMap => CompleteOp(item, itemMap.recursiveChildren(item.id)))

    def onNotebookClickAdd(item: Item) = {
      t.modState(s =>
        s.copy(selectedNotebook = if (!s.isAdding) Some(item.id) else s.selectedNotebook,
          isAdding = if (s.selectedNotebook == Some(item.id)) !s.isAdding else true))
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

  val dashboard = ReactComponentB[MainRouter.Router]("Dashboard")
    .initialStateP(router =>
      State(selectedNotebook = items.notebooks().headOption.map(_.id))
    )
    .backend(new Backend(_))
    .render((router, S, B) => {
      val appLinks = MainRouter.appLinks(router)
      <.div(
        Header(),
        Sidebar(Sidebar.Props(B, S.itemMap, S.selectedNotebook, S.isAdding)),
        Contents(Contents.Props(B, S.itemMap, S.selectedNotebook, S.isAdding))
      )
    }).build
}
