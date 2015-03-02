package lodo

import java.util.UUID

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

import Helper._

object NotebookSelector {
  case class Props(b: Dashboard.Backend, itemMap: ItemMap,
                   selectedNotebook: Option[UUID],
                   isAdding: Boolean)

  case class State(isNotebookAdding: Boolean = false,
                   addText: String = "",
                   isDragOver: Boolean = false)

  class Backend(t: BackendScope[Props, State]) {
    def onClickAdd() = {
      t.modState(s => s.copy(isNotebookAdding = !s.isNotebookAdding))
    }

    def onFocus(e: ReactEventI) =
      e.currentTarget.select()

    def onEdit(e: ReactEventI) =
      t.modState(s => s.copy(addText = e.currentTarget.value))

    def onSubmit(e: ReactEvent) = {
      e.preventDefault()
      t.modState(s => {
        t.props.b.onNotebookAddComplete(AddOp(Item(UUID.randomUUID, None, time(), s.addText)))
        s.copy(isNotebookAdding = false, addText = "")
      })
    }

    def onDragEnter(e: ReactDragEvent) =
      t.modState(_.copy(isDragOver = true))

    def onDragLeave(e: ReactDragEvent) =
      t.modState(_.copy(isDragOver = false))

    def onDragOver(e: ReactDragEvent) = {
      e.stopPropagation()
      e.preventDefault()
    }

    def onDrop(e: ReactDragEvent): Unit = {
      e.stopPropagation()
      e.preventDefault()

      val src = UUID.fromString(e.dataTransfer.getData("lodo"))

      val item = t.props.itemMap(Some(src))

      t.modState(_.copy(isDragOver = false))

      item.map(item => {
        t.props.b.moveAndSelectNotebook(MoveOp(item, None), item.id)
      })
    }
  }

  val notebookSelector = ReactComponentB[Props]("NotebookSelector")
    .initialState(State())
    .backend(new Backend(_))
    .render((P, S, B) =>
      <.ul(^.cls := "nav nav-sidebar",
        P.itemMap.notebooks()
          .zipWithIndex
          .map { case (c, i) =>
            Notebook(Notebook.Props(P.b, P.selectedNotebook, P.isAdding, P.itemMap, c, i))
          },
        if (S.isNotebookAdding)
          <.li(
            <.a(^.href := "#",
              <.form(^.onSubmit ==> B.onSubmit,
                <.input(^.onFocus ==> B.onFocus, ^.autoFocus := true,
                  ^.onChange ==> B.onEdit)
              )
            )
          )
        else
          <.li(
            ^.classSet(("dragover", S.isDragOver)),
            ^.onDragEnter ==> B.onDragEnter,
            ^.onDragLeave ==> B.onDragLeave,
            ^.onDragOver ==> B.onDragOver,
            ^.onDrop ==> B.onDrop,
              <.a(^.href := "#", ^.onClick --> B.onClickAdd(),
                <.span(^.cls := "glyphicon glyphicon-plus"),
                <.span(^.classSet(("draghidden", !S.isDragOver)),
                  " Create notebook from item"
                )
              )
          )
      )
    )
    .build

  def apply(props: Props) = notebookSelector(props)
}
