package lodo

import java.util.UUID

import japgolly.scalajs.react.{ReactEventI, BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._
import lodo.Helper._

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

    def onEditChange(e: ReactEventI) =
      t.modState(s => s.copy(editText = e.currentTarget.value))

    def onAddChange(e: ReactEventI) =
      t.modState(s => s.copy(addText = e.currentTarget.value))

    def onFocus(e: ReactEventI) =
      e.currentTarget.select()

    def onEditSubmit(item: Item) =
      t.modState(s => {
        t._props.b.applyOperation(EditOp(item, s.editText))
        s.copy(isEditing = !s.isEditing)
      })

    def onAddSubmit() =
      t.modState(s => {
        t.props.b.applyOperation(AddOp(Item(UUID.randomUUID, Some(t.props.item.id), time(), s.addText)))
        s.copy(isAdding = false, addText = "")
      })
  }

  val list = ReactComponentB[Props]("List")
    .initialStateP(P => State(editText = P.item.contents))
    .backend(new Backend(_))
    .render((P, S, B) => {
      val children = P.itemMap.children(P.item.id)
      <.div(^.cls := "panel panel-default item",
        <.div(^.cls := (if (children.nonEmpty || S.isAdding) "panel-heading" else "panel-body"),
          ^.draggable := true,
          <.span(^.cls := "sel-num", P.index),
          <.span(^.cls := "content",
            if (S.isEditing)
              <.form(^.onSubmit --> B.onEditSubmit(P.item),
                <.input(^.onFocus ==> B.onFocus, ^.autoFocus := true,
                  ^.defaultValue := P.item.contents, ^.onChange ==> B.onEditChange)
              )
            else
              P.item.contents
          ),
          BtnGroup(
            BtnGroup.Props(P.item, "item",
              S.isEditing, S.isAdding,
              P.b.onClickComplete,
              B.onClickEdit,
              B.onClickAdd
            )
          )
        ),
        (children.nonEmpty || S.isAdding) ?= <.div(^.cls := "panel-body",
          children.nonEmpty ?= children
            .zipWithIndex
            .map { case (c, i) =>
              LodoList(LodoList.Props(P.b, P.itemMap, c, i))
            },
          S.isAdding ?= <.div(
            <.a(^.href := "#",
              <.form(^.onSubmit --> B.onAddSubmit(),
                <.input(^.onFocus ==> B.onFocus, ^.autoFocus := true,
                  ^.onChange ==> B.onAddChange)
              )
            )
          )
        )
      )
    }).build

  def apply(props: Props): TagMod = list(props)
}
