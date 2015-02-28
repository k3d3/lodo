package lodo

import java.util.UUID

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object Sidebar {
  case class Props(b: Dashboard.Backend, itemMap: ItemMap, selectedNotebook: Option[UUID] = None, isAdding: Boolean = false)

  val sidebar = ReactComponentB[Props]("Sidebar")
    .render({ P =>
      <.div(^.cls := "container-fluid",
        <.div(^.cls := "row",
          <.div(^.cls := "col-sm-4 col-md-3 col-lg-2 sidebar",
            NotebookSelector(NotebookSelector.Props(P.b, P.itemMap, P.selectedNotebook, P.isAdding))
          )
        )
      )
    }).build

  def apply(props: Props) = sidebar(props)
}
