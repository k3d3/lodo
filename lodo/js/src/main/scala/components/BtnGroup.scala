package lodo

import japgolly.scalajs.react.{BackendScope, ReactEventH, ReactComponentB}
import japgolly.scalajs.react.vdom.prefix_<^._
import org.scalajs.dom.raw.HTMLButtonElement

object BtnGroup {
  sealed trait BtnType
  object BtnComplete extends BtnType
  object BtnEdit extends BtnType
  object BtnAdd extends BtnType


  case class Props(item: Item, btnGroupType: String,
                   onClickComplete: Item => Unit,
                   onClickEdit: Item => Unit,
                   onClickAdd: Item => Unit)

  case class State(isEditing: Boolean = false)

  class Backend(t: BackendScope[Props, State]) {
    def onClickEdit(item: Item) = {
      t.modState(s => s.copy(isEditing = !s.isEditing))
      t.props.onClickEdit(item)
    }
  }

  def btn(P: Props, S: State, B: Backend, btnType: BtnType, title: String) = {
    val glyphClass = btnType match {
      case BtnComplete => "ok"
      case BtnEdit => if (S.isEditing) "edit" else "pencil"
      case BtnAdd => "plus"
    }
    <.button(^.cls := "btn btn-sm btn-default", ^.title := title,
      <.span(^.cls := "glyphicon glyphicon-" + glyphClass),
      ^.onClick ==> { e: ReactEventH =>
        e.stopPropagation()
        btnType match {
          case BtnComplete => P.onClickComplete(P.item)
          case BtnEdit => B.onClickEdit(P.item)
          case BtnAdd => P.onClickAdd(P.item)
        }
      }
    )
  }

  val btnGroup = ReactComponentB[Props]("BtnGroup")
    .initialState(State())
    .backend(new Backend(_))
    .render((P, S, B) => {
      <.span(^.cls := s"pull-right btn-group ${P.btnGroupType}-buttons",
        btn(P, S, B, BtnComplete, "Completed"),
        btn(P, S, B, BtnEdit, "Edit"),
        btn(P, S, B, BtnAdd, "Add")
      )
    }).build

  def apply(props: Props) = btnGroup(props)
}
