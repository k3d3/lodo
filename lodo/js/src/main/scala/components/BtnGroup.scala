package lodo

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

object BtnGroup {
  sealed trait BtnType
  object BtnComplete extends BtnType
  object BtnEdit extends BtnType
  object BtnAdd extends BtnType


  case class Props(item: Item, btnGroupType: String,
                   isEditing: Boolean, isAdding: Boolean,
                   onClickComplete: Item => Unit,
                   onClickEdit: Item => Unit,
                   onClickAdd: Item => Unit)

  def btn(P: Props, btnType: BtnType, title: String) = {
    val glyphClass = btnType match {
      case BtnComplete => "remove"
      case BtnEdit => if (P.isEditing) "edit" else "pencil"
      case BtnAdd => if (P.isAdding) "check" else "plus"
    }
    <.button(^.cls := "btn btn-sm btn-default", ^.title := title,
      <.span(^.cls := "glyphicon glyphicon-" + glyphClass),
      ^.onClick ==> { e: ReactEventH =>
        e.stopPropagation()
        btnType match {
          case BtnComplete => P.onClickComplete(P.item)
          case BtnEdit => P.onClickEdit(P.item)
          case BtnAdd => P.onClickAdd(P.item)
        }
      }
    )
  }

  val btnGroup = ReactComponentB[Props]("BtnGroup")
    .render(P => {
      <.span(^.cls := s"pull-right btn-group ${P.btnGroupType}-buttons",
        btn(P, BtnComplete, "Completed"),
        btn(P, BtnEdit, "Edit"),
        btn(P, BtnAdd, "Add")
      )
    }).build

  def apply(props: Props) = btnGroup(props)
}
