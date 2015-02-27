package lodo

import japgolly.scalajs.react.{ReactEventI, BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._

object LodoList {
  case class Props(b: Dashboard.Backend, itemMap: ItemMap, item: Item, index: Int)

  case class State(isAdding: Boolean = false,isEditing: Boolean = false,
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

    def onFocus(e: ReactEventI) =
      e.currentTarget.select()

    def onSubmit(item: Item) =
      t.modState(s => {
        t._props.b.applyOperation(EditOp(item, s.editText))
        s.copy(isEditing = !s.isEditing)
      })
  }

  val list = ReactComponentB[Props]("List")
    .initialStateP(P => State(editText = P.item.contents))
    .backend(new Backend(_))
    .render((P, S, B) => {
      val children = P.itemMap.children(P.item.id)
      <.div(^.cls := "panel panel-default item",
        <.div(^.cls := (if (children.isEmpty) "panel-body" else "panel-heading"),
          ^.draggable := true,
          <.span(^.cls := "sel-num", P.index),
          <.span(^.cls := "content",
            if (S.isEditing)
              <.form(^.onSubmit --> B.onSubmit(P.item),
                <.input(^.onFocus ==> B.onFocus, ^.autoFocus := true,
                  ^.defaultValue := P.item.contents, ^.onChange ==> B.onEdit)
              )
            else
              P.item.contents
          ),
          BtnGroup(
            BtnGroup.Props(P.item, "item", S.isEditing,
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
              LodoList(LodoList.Props(P.b, P.itemMap, c, i))
            }
        )
      )
    }).build

  def apply(props: Props): TagMod = list(props)
}
