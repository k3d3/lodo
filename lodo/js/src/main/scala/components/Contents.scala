package lodo

import java.util.UUID

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object Contents {
  case class Props(b: Dashboard.Backend, itemMap: ItemMap, selectedNotebook: Option[UUID], isAdding: Boolean)

  val contents = ReactComponentB[Props]("Contents")
    .render({ P =>
      <.div(^.id := "lodo-contents",
        ^.cls := "col-sm-8 col-sm-offset-4 col-md-9 col-md-offset-3 col-lg-10 col-lg-offset-2 main",
        "Currently selected notebook:", P.selectedNotebook.toString,
        P.selectedNotebook.map(n =>
          P.itemMap
            .children(n)
            .zipWithIndex
            .map{ case (p, i) => Page(Page.Props(P.b, P.itemMap, p, i)) }
        ),
        P.isAdding ?= <.div("Hello")
      )
    }).build

  def apply(props: Props) = contents(props)
}
