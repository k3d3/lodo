package lodo

import java.util.UUID

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object Sidebar {
  case class Props(items: Seq[Item], selectNotebook: Item => Unit, selectedNotebook: Option[Item] = None)

  val sidebar = ReactComponentB[Props]("Sidebar")
    .render(P => {
      <.div(^.cls := "container-fluid",
        <.div(^.cls := "row",
          <.div(^.cls := "col-sm-4 col-md-3 col-lg-2 sidebar",
            NotebookSelector(NotebookSelector.Props(P.items, P.selectNotebook, P.selectedNotebook))
          )
        )
      )
    }).build

  def apply(props: Props) = sidebar(props)
}
