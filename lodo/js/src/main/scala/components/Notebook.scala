package lodo

import java.util.UUID

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

object Notebook {
  case class Props(b: Dashboard.Backend, selectedNotebook: Option[UUID],
                   isAdding: Boolean, itemMap: ItemMap, item: Item, index: Int)

  case class State(isEditing: Boolean = false,
                   addText: String = "", editText: String)

  class Backend(t: BackendScope[Props, State]) {
    def onClickEdit(item: Item) = {
      t.modState(s => {
        if (s.isEditing)
          t.props.b.applyOperation(EditOp(item, s.editText))
        s.copy(isEditing = !s.isEditing)
      })
    }

    def onClickAdd(item: Item) = {
      t.props.b.onNotebookClickAdd(item)
    }

    def onEdit(e: ReactEventI) =
      t.modState(s => s.copy(editText = e.currentTarget.value))

    def onFocus(e: ReactEventI) =
      e.currentTarget.select()

    def onSubmit(item: Item)(e: ReactEvent) = {
      e.preventDefault()
      t.modState(s => {
        t.props.b.applyOperation(EditOp(item, s.editText))
        s.copy(isEditing = false)
      })
    }
  }

  val notebook = ReactComponentB[Props]("Notebook")
    .initialStateP(P => State(editText = P.item.contents))
    .backend(new Backend(_))
    .render((P, S, B) => {
      <.li(^.key := P.item.id.toString,
        ^.draggable := !S.isEditing,
        (P.selectedNotebook == Some(P.item.id)) ?= (^.cls := "active"),
        <.a(^.href := "#", ^.onClick --> P.b.selectNotebook(P.item),
          <.span(^.cls := "sel-num", P.index),
          <.span(^.cls := "content",
            if (S.isEditing)
              <.form(^.onSubmit ==> B.onSubmit(P.item),
                <.input(^.onFocus ==> B.onFocus, ^.autoFocus := true,
                  ^.defaultValue := P.item.contents, ^.onChange ==> B.onEdit)
              )
            else
              P.item.contents
          ),
          BtnGroup(
            BtnGroup.Props(P.item, "page",
              S.isEditing, if (P.selectedNotebook == Some(P.item.id)) P.isAdding else false,
              P.b.onClickComplete,
              B.onClickEdit,
              B.onClickAdd
            )
          )
        )
      )
    }).build


  def apply(props: Props) = notebook(props)
}
