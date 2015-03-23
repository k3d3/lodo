package lodo

import java.util.UUID

import eu.henkelmann.actuarius.ActuariusTransformer
import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.{ReactMouseEventH, ReactEvent, BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom

import autowire._
import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

import Helper._

object Dashboard {

  case class State(itemMap: ItemMap = new ItemMap(),
                   selectedNotebook: Option[UUID] = None,
                   isAdding: Boolean = false,
                   isSidebarShown: Boolean = false,
                   isCompleteHidden: Boolean = false,
                   lastOp: Int = 0, sessId: UUID = new UUID(0, 0))
  // Note that isSidebarShown is actually inverted for non-mobile

  class Backend(t: BackendScope[MainRouter.Router, State]) extends OnUnmount {
    def onInit(): Unit = {
      Client[LodoApi].getItems("mainUser").call().foreach { case (items: Seq[Item], lastOp: Int, sessId: UUID) =>
        val itemMap = new ItemMap(items)
        t.modState(s => s.copy(
          itemMap = itemMap,
          selectedNotebook = itemMap.notebooks().headOption.map(_.id),
          lastOp = lastOp,
          sessId = sessId)
        )
        updateHandles.foreach(dom.clearTimeout)
        updateHandles = List()
        updateHandles :+ org.scalajs.dom.setTimeout(() => checkForUpdates(), 0)
      }
    }

    onInit()

    var updateHandles: List[Int] = List()
    def checkForUpdates(): Unit = {
      val call = Client[LodoApi].getChanges(t.state.lastOp, t.state.sessId).call()
      call.onSuccess({ case changeOption: Option[List[OpType]] =>
        changeOption match {
          case None =>
            dom.location.reload() // TODO: For development only
            //onInit()
          case Some(changes) =>
            if (changes.nonEmpty)
              t.modState(s => {
                val itemMap = changes.foldLeft(s.itemMap)((m, i) =>
                  i match {
                    case OpApply(op) => m(op) 
                    case OpUndo(op) => m.undo(op)
                  })
                s.copy(itemMap = itemMap,
                  lastOp = s.lastOp + changes.length,
                  selectedNotebook =
                    if (s.selectedNotebook == None)
                      itemMap.notebooks().headOption.map(_.id)
                    else
                      s.selectedNotebook
                )
              })
        }

        updateHandles.foreach(dom.clearTimeout)
        updateHandles = List()
        updateHandles :+ dom.setTimeout(() => checkForUpdates(), 0)
      })
      call.onFailure({ case _ =>
        updateHandles.foreach(dom.clearTimeout)
        updateHandles = List()
        updateHandles :+ dom.setTimeout(() => checkForUpdates(), 1000)
      })
    }

    def selectNotebook(item: Item) =
      t.modState(s =>
        s.copy(selectedNotebook = Some(item.id),
          isAdding = if (s.selectedNotebook == Some(item.id)) s.isAdding else false))

    def moveAndSelectNotebook(op: MoveOp, itemId: UUID) =
      t.modState { s =>
        val newItemMap = s.itemMap(op)
        Client[LodoApi].applyOperation(op).call()
        newItemMap
          .getNotebook(itemId)
          .fold(s.copy(itemMap = newItemMap)) { n =>
            s.copy(
              selectedNotebook = Some(n.id),
              itemMap = newItemMap
            )
          }
      }

    def toggleShowSidebar() = {
      t.modState(s => s.copy(isSidebarShown = !s.isSidebarShown))
    }

    def toggleCompleted() = {
      t.modState(s => s.copy(isCompleteHidden = !s.isCompleteHidden))
    }

    def performUndo()(e: ReactEvent): Future[Boolean] = Future {
      e.preventDefault()
      e.stopPropagation()
      Client[LodoApi].undo().call()
      false
    }

    def performRedo()(e: ReactEvent): Future[Boolean] = Future {
      e.preventDefault()
      e.stopPropagation()
      Client[LodoApi].redo().call()
      false
    }

    def onClickComplete(item: Item) = {
      t.modState(s => {
        var op = CompleteOp(item, !item.completed)
        Client[LodoApi].applyOperation(op).call()
        s.copy(itemMap = s.itemMap(op))
      })
    }

    def onClickFold(item: Item) = {
      t.modState(s => {
        var op = FoldOp(item, !item.folded)
        Client[LodoApi].applyOperation(op).call()
        s.copy(itemMap = s.itemMap(op))
      })
    }

    def onClickRemove(item: Item) = {
      if (item.parent == None)
        t.modState(s => {
          val op = RemoveOp(item, s.itemMap.recursiveChildren(item.id))
          Client[LodoApi].applyOperation(op).call()
          val newItemMap = s.itemMap(op)
          s.copy(
            selectedNotebook = newItemMap.notebooks().headOption.map(_.id),
              itemMap = newItemMap
          )
        })
      else
        applyOperation(itemMap => RemoveOp(item, itemMap.recursiveChildren(item.id)))
    }

    def onNotebookClickAdd(item: Item) = {
      t.modState(s =>
        s.copy(selectedNotebook = Some(item.id),
          isAdding = if (s.selectedNotebook == Some(item.id)) !s.isAdding else true))
    }

    def onAddComplete(op: AddOp) = {
      Client[LodoApi].applyOperation(op).call()
      t.modState(s => s.copy(
        isAdding = false,
        itemMap = s.itemMap(op)
      ))
    }

    def onNotebookAddComplete(op: AddOp) = {
      Client[LodoApi].applyOperation(op).call()
      t.modState(s => s.copy(
        isAdding = false,
        itemMap = s.itemMap(op),
        selectedNotebook = Some(op.item.id)
      ))
    }

    def applyOperation(op: Op): Unit = applyOperation(_ => op)

    def applyOperation(opBuild: ItemMap => Op) = {
      t.modState(s => {
        val op = opBuild(s.itemMap)
        Client[LodoApi].applyOperation(op).call()
        s.copy(itemMap = s.itemMap(op))
      })
    }

    val mdTransformer = new ActuariusTransformer()
    def mdTransform(input: String): String = {
      // Ensure URLs are converted to links
      mdTransformer(
        input.replaceAll("(\\A|\\s|^)((http|https):\\S+)(\\s|\\z|$)", "$1[$2]($2)$4")
      ).toString
    }
  }

  val dashboard = ReactComponentB[MainRouter.Router]("Dashboard")
    .initialState(State())
    .backend(new Backend(_))
    .render((router, S, B) => {
      val appLinks = MainRouter.appLinks(router)
      <.div(
        Header(Header.Props(B)),
        Sidebar(Sidebar.Props(B, S.itemMap, S.selectedNotebook, S.isAdding, S.isSidebarShown, S.isCompleteHidden)),
        Contents(Contents.Props(B, S.itemMap, S.selectedNotebook, S.isAdding, !S.isSidebarShown, S.isCompleteHidden))
      )
    }).build
}
