package lodo

import java.util.UUID

import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._

object NotebookSelector {
  case class Props(b: Dashboard.Backend, itemMap: ItemMap, selectedNotebook: Option[UUID] = None)

  case class State(isEditing: Boolean = false, isAdding: Boolean = false)

  class Backend(t: BackendScope[Props, State]) {
    def onClickEdit(item: Item) = {
      t.modState(s => s.copy(isEditing = !s.isEditing))
    }
  }

  val notebookSelector = ReactComponentB[Props]("NotebookSelector")
    .initialState(State())
    .backend(new Backend(_))
    .render((P, S, B) => <.ul(^.cls := "nav nav-sidebar", P.itemMap.notebooks()
      .zipWithIndex
      .map { case (c, i) =>
        Notebook(Notebook.Props(P.b, P.selectedNotebook, P.itemMap, c, i))
      }
    ))
    .build

  def apply(props: Props) = notebookSelector(props)
}
