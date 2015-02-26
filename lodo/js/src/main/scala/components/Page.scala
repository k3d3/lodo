package lodo

import java.util.UUID

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object Page {
  case class Props(itemMap: ItemMap, item: Item, index: Int)

  val page = ReactComponentB[Props]("Page")
    .render(P => {
      val children = P.itemMap.children(P.item.id)
      <.div(^.cls := "panel panel-info page",
        <.div(^.cls := "panel-heading",
          ^.draggable := true,
          <.span(^.cls := "sel-num", P.index),
          <.span(^.cls := "content", P.item.contents)
        )
      )
    }).build

  def apply(props: Props) = page(props)
}
