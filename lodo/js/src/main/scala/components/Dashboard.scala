package lodo

import java.util.UUID

import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._

import autowire._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

import Helper._

object Dashboard {

  case class State(itemMap: ItemMap = new ItemMap(),
                   selectedNotebook: Option[UUID] = None,
                   isAdding: Boolean = false,
                   isSidebarShown: Boolean = false,
                   undoStack: List[Op] = List(),
                   redoStack: List[Op] = List())

  class Backend(t: BackendScope[MainRouter.Router, State]) extends OnUnmount {
    AjaxClient[LodoApi].getItems("test").call().foreach { items: Seq[Item] =>
      val itemMap = new ItemMap(items)
      t.modState(s => s.copy(
        itemMap = itemMap,
        selectedNotebook = itemMap.notebooks().headOption.map(_.id)))
    }

    def selectNotebook(item: Item) = {
      t.modState(s =>
        s.copy(selectedNotebook = Some(item.id),
          isAdding = if (s.selectedNotebook == Some(item.id)) s.isAdding else false))
    }

    def toggleShowSidebar() = {
      println("toggle")
      t.modState(s => s.copy(isSidebarShown = !s.isSidebarShown))
    }

    def performUndo() = {
      t.modState(s =>
        if (s.undoStack.isEmpty)
          s
        else
          s.copy(
            undoStack = s.undoStack.tail,
            redoStack = s.undoStack.head :: s.redoStack,
            itemMap = s.itemMap.undo(s.undoStack.head)
          )
      )
    }

    def performRedo() = {
      t.modState(s =>
        if (s.redoStack.isEmpty)
          s
        else
          s.copy(
            undoStack = s.redoStack.head :: s.undoStack,
            redoStack = s.redoStack.tail,
            itemMap = s.itemMap(s.redoStack.head)
          )
      )
    }

    def onClickComplete(item: Item) = {
      if (item.parent == None)
        t.modState(s => {
          val op = CompleteOp(item, s.itemMap.recursiveChildren(item.id))
          val newItemMap = s.itemMap(op)
          s.copy(
            selectedNotebook = newItemMap.notebooks().headOption.map(_.id),
            itemMap = newItemMap,
            undoStack = op :: s.undoStack,
            redoStack = List.empty)
        })
      else
        applyOperation(itemMap => CompleteOp(item, itemMap.recursiveChildren(item.id)))
    }

    def onNotebookClickAdd(item: Item) = {
      t.modState(s =>
        s.copy(selectedNotebook = Some(item.id),
          isAdding = if (s.selectedNotebook == Some(item.id)) !s.isAdding else true))
    }

    def onAddComplete(op: AddOp) = {
      t.modState(s => s.copy(
        isAdding = false,
        itemMap = s.itemMap(op),
        undoStack = op :: s.undoStack,
        redoStack = List.empty
      ))
    }

    def onNotebookAddComplete(op: AddOp) = {
      t.modState(s => s.copy(
        isAdding = false,
        itemMap = s.itemMap(op),
        undoStack = op :: s.undoStack,
        redoStack = List.empty,
        selectedNotebook = Some(op.item.id)
      ))
    }

    def applyOperation(op: Op): Unit = applyOperation(_ => op)

    def applyOperation(opBuild: ItemMap => Op) = {
      t.modState(s => {
        val op = opBuild(s.itemMap)
        s.copy(itemMap = s.itemMap(op),
          undoStack = op :: s.undoStack,
          redoStack = List.empty)
      })
    }
  }

  val dashboard = ReactComponentB[MainRouter.Router]("Dashboard")
    .initialState(State())
    .backend(new Backend(_))
    .render((router, S, B) => {
      val appLinks = MainRouter.appLinks(router)
      <.div(
        Header(Header.Props(B)),
        Sidebar(Sidebar.Props(B, S.itemMap, S.selectedNotebook, S.isAdding, S.isSidebarShown)),
        Contents(Contents.Props(B, S.itemMap, S.selectedNotebook, S.isAdding))
      )
    }).build
}
