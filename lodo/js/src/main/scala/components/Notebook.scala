package lodo

import java.util.UUID

import japgolly.scalajs.react.{Ref, ReactEventI, BackendScope, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.raw.HTMLInputElement

object Notebook {
  case class Props(b: Dashboard.Backend, selectedNotebook: Option[UUID],
                   itemMap: ItemMap, item: Item, index: Int)

  case class State(isAdding: Boolean = false, isEditing: Boolean = false,
                   addText: String = "", editText: String)

  class Backend(t: BackendScope[Props, State]) {
    def onClickEdit(item: Item) = {
      t.modState(s => {
        if (s.isEditing)
          t.props.b.applyOperation(EditOp(item, s.editText))
        s.copy(isEditing = !s.isEditing)
      })
    }

    def onClickAdd(item: Item) =
      t.modState(s => s.copy(isAdding = !s.isAdding))

    def onEdit(e: ReactEventI) =
      t.modState(s => s.copy(editText = e.currentTarget.value))

    def onFocus(e: ReactEventI) = {
      e.currentTarget.select()
    }

    def onSubmit(item: Item) =
      t.modState(s => {
        t._props.b.applyOperation(EditOp(item, s.editText))
        s.copy(isEditing = !s.isEditing)
      })
  }

  val notebook = ReactComponentB[Props]("Notebook")
    .initialStateP(P => State(editText = P.item.contents))
    .backend(new Backend(_))
    .render((P, S, B) => {
      <.li(^.key := P.item.id.toString,
        (P.selectedNotebook == Some(P.item.id)) ?= (^.cls := "active"),
        <.a(^.href := "#", ^.onClick --> P.b.selectItem(P.item),
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
            BtnGroup.Props(P.item, "page",
              P.b.onClickComplete,
              B.onClickEdit,
              P.b.onNotebookClickAdd
            )
          )
        )
      )
    }).build


  def apply(props: Props) = notebook(props)
}
