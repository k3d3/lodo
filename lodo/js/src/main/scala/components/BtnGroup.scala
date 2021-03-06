/*
Lodo is a layered to-do list (Outliner)
Copyright (C) 2015 Keith Morrow.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License v3 as
published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package lodo

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

object BtnGroup {
  sealed trait BtnType
  object BtnComplete extends BtnType
  object BtnEdit extends BtnType
  object BtnAdd extends BtnType
  object BtnRemove extends BtnType

  case class Props(item: Item, btnGroupType: String,
                   isEditing: Boolean, isAdding: Boolean, isComplete: Boolean,
                   onClickComplete: Item => Unit,
                   onClickEdit: Item => Unit,
                   onClickAdd: Item => Unit,
                   onClickRemove: Item => Unit)

  def btn(P: Props, btnType: BtnType, title: String) = {
    val glyphClass = btnType match {
      case BtnComplete => if (P.isComplete) "ok-circle" else "ok"
      case BtnEdit => if (P.isEditing) "edit" else "pencil"
      case BtnAdd => if (P.isAdding) "check" else "plus"
      case BtnRemove => "remove"
    }
    <.button(^.cls := "btn btn-sm btn-default", ^.title := title,
      <.span(^.cls := "glyphicon glyphicon-" + glyphClass),
      ^.onClick ==> { e: ReactEventH =>
        e.stopPropagation()
        btnType match {
          case BtnComplete => P.onClickComplete(P.item)
          case BtnEdit => P.onClickEdit(P.item)
          case BtnAdd => P.onClickAdd(P.item)
          case BtnRemove => P.onClickRemove(P.item)
        }
      }
    )
  }

  val btnGroup = ReactComponentB[Props]("BtnGroup")
    .render(P => {
      <.span(^.cls := s"pull-right btn-group ${P.btnGroupType}-buttons",
        btn(P, BtnComplete, "Complete"),
        btn(P, BtnRemove, "Remove"),
        btn(P, BtnEdit, "Edit"),
        btn(P, BtnAdd, "Add")
      )
    }).build

  def apply(props: Props) = btnGroup(props)
}
