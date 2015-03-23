package lodo

import java.util.UUID

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import lodo.Helper._

object Contents {
  case class Props(b: Dashboard.Backend, itemMap: ItemMap, selectedNotebook: Option[UUID],
                   isAdding: Boolean, isSidebarShown: Boolean)

  case class State(addText: String = "", isDragOver: Boolean = false)

  class Backend(t: BackendScope[Props, State]) {
    def onFocus(e: ReactEventI) =
      e.currentTarget.select()

    def onAddChange(e: ReactEventI) =
      t.modState(s => s.copy(addText = e.currentTarget.value))

    def onAddSubmit(e: ReactEvent) = {
      e.preventDefault()
      t.modState(s => {
        t.props.b.onAddComplete(AddOp(Item(UUID.randomUUID, t.props.selectedNotebook, time(), s.addText)))
        s.copy(addText = "")
      })
    }

    def onDragEnter(e: ReactDragEvent) =
      t.modState(_.copy(isDragOver = true))

    def onDragLeave(e: ReactDragEvent) =
      t.modState(_.copy(isDragOver = false))

    def onDragOver(e: ReactDragEvent) = {
      t.modState(_.copy(isDragOver = true))
      e.stopPropagation()
      e.preventDefault()
    }

    def onDrop(e: ReactDragEvent): Unit = {
      e.stopPropagation()
      e.preventDefault()
      t.modState(_.copy(isDragOver = false))

      val src = UUID.fromString(e.dataTransfer.getData("lodo"))
      val dst = t.props.selectedNotebook

      if (Some(src) == dst)
        return // Don't allow drop on self

      t.props.itemMap(Some(src)).map(item => {
        t.props.b.applyOperation(MoveOp(item, dst, time()))
      })
    }

    def onClickAdd(e: ReactMouseEvent): Unit = {
      t.props.itemMap(t.props.selectedNotebook).foreach { item =>
        t.props.b.onNotebookClickAdd(item)
      }
    }
  }

  val contents = ReactComponentB[Props]("Contents")
    .initialState(State())
    .backend(new Backend(_))
    .render((P, S, B) => {
      <.div(^.id := "lodo-contents",
        ^.classSet(
          ("dragover", S.isDragOver)
        ),
        ^.onDragEnter ==> B.onDragEnter,
        ^.onDragLeave ==> B.onDragLeave,
        ^.onDragOver ==> B.onDragOver,
        ^.onDrop ==> B.onDrop,
        ^.classSet1("main",
          ("col-sm-8 col-sm-offset-4 col-md-9 col-md-offset-3", P.isSidebarShown),
          ("col-sm-12 col-md-12", !P.isSidebarShown)
        ),
        P.itemMap(P.selectedNotebook).map(item =>
          P.itemMap
            .children(item.id)
            .zipWithIndex
            .map {
              case (p, i) =>
                Page(
                  Page.Props(P.b, P.itemMap, p, i, P.isSidebarShown, item.completed)
                )
            }
        ),
        if (P.isAdding)
          <.div(
            <.a(^.href := "#",
              <.form(^.onSubmit ==> B.onAddSubmit,
                <.input(^.onFocus ==> B.onFocus, ^.autoFocus := true,
                  ^.onChange ==> B.onAddChange)
              )
            )
          )
        else
          (P.selectedNotebook != None) ?= <.div(^.cls := "btn btn-default",
            ^.onClick ==> B.onClickAdd,
            "Add new page"
          )
      )
    }).build

  def apply(props: Props) = contents(props)
}
