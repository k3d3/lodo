package lodo

import java.util.UUID

import japgolly.scalajs.react.extra.OnUnmount
import japgolly.scalajs.react.{BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._

object NotebookSelector {
  case class Props(items: Seq[Item], selectItem: Item => Unit, selectedNotebook: Option[UUID] = None)

  def renderItem(P: Props)(item: Item, index: Int) = {
    <.li(^.key := item.id.toString,
      (P.selectedNotebook == Some(item.id)) ?= (^.cls := "active"),
      <.a(^.href := "#", ^.onClick --> P.selectItem(item),
        <.span(^.cls := "sel-num", index),
        <.span(^.cls := "content", item.contents)
      ))
  }

  def allItems(P: Props) = {
    P.items
      .zipWithIndex
      .map((renderItem(P) _).tupled)
  }

  val notebookSelector = ReactComponentB[Props]("NotebookSelector")
    .render(P => <.ul(^.cls := "nav nav-sidebar", allItems(P)))
    .build

  def apply(props: Props) = notebookSelector(props)
}
