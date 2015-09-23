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

object SelNum {
  case class Props(index: Int, isFolded: Boolean, toggleFold: ReactMouseEvent => Unit)

  val selNum = ReactComponentB[Props]("SelNum")
    .render(P => {
    <.span(^.cls := "sel-num",
      ^.onClick ==> P.toggleFold,
      if (P.isFolded)
        <.span(^.cls := "foldstatus glyphicon glyphicon-plus")
      else
        <.span(^.cls := "foldstatus glyphicon glyphicon-minus"),
      <.span(^.cls := "notfoldstatus", P.index)
    )
    }).build

  def apply(props: Props): TagMod = selNum(props)
}
