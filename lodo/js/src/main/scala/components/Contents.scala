package lodo

import java.util.UUID

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object Contents {
  case class Props(itemMap: ItemMap, selectedNotebook: Option[Item])

  val contents = ReactComponentB[Props]("Contents")
    .render({ P =>
      <.div(^.id := "lodo-contents",
        ^.cls := "col-sm-8 col-sm-offset-4 col-md-9 col-md-offset-3 col-lg-10 col-lg-offset-2 main",
        "Currently selected notebook:", P.selectedNotebook.map(_.id).toString,
        P.selectedNotebook.map(n =>
          P.itemMap
            .children(n.id)
            .zipWithIndex
            .map{ case (p, i) => Page(Page.Props(P.itemMap, p, i)) }
        )
      )
    }).build

  def apply(props: Props) = contents(props)
}
