package lodo

import japgolly.scalajs.react.{ReactEventI, BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._

object Page {
  case class Props(b: Dashboard.Backend, itemMap: ItemMap, item: Item, index: Int)

  case class State(isAdding: Boolean = false, isEditing: Boolean = false,
                   addText: String = "", editText: String)

  class Backend(t: BackendScope[Props, State]) {
    def onClickEdit(item: Item) =
      t.modState(s => {
        if (s.isEditing)
          t._props.b.applyOperation(EditOp(item, s.editText))
        s.copy(isEditing = !s.isEditing)
      })

    def onClickAdd(item: Item) =
      t.modState(s => s.copy(isAdding = !s.isAdding))

    def onEdit(e: ReactEventI) =
      t.modState(s => s.copy(editText = e.currentTarget.value))
  }

  val page = ReactComponentB[Props]("Page")
    .initialStateP(P => State(editText = P.item.contents))
    .backend(new Backend(_))
    .render((P, S, B) => {
      val children = P.itemMap.children(P.item.id)

      <.div(^.key := P.item.id.toString,
        ^.cls := "panel panel-info page",
        <.div(^.cls := "panel-heading",
          ^.draggable := true,
          <.span(^.cls := "sel-num", P.index),
          <.span(^.cls := "content",
            if (S.isEditing)
              <.input(^.value := P.item.contents)
            else
              P.item.contents
          ),
          BtnGroup(
            BtnGroup.Props(P.item, "page",
              P.b.onClickComplete,
              B.onClickEdit,
              B.onClickAdd
            )
          )
        ),
        children.nonEmpty ?= <.div(^.cls := "panel-body",
          children
            .zipWithIndex
            .map { case (c, i) =>
              <.div(^.cls := "col-sm-6",
                LodoList(LodoList.Props(P.b, P.itemMap, c, i))
              )
            }
        )
      )
    }).build

  def apply(props: Props) = page(props)
}
