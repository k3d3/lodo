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

import java.util.UUID

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object Sidebar {
  case class Props(b: Dashboard.Backend, itemMap: ItemMap,
                   selectedNotebook: Option[UUID] = None,
                   isAdding: Boolean = false,
                   isSidebarShown: Boolean = false,
                   isCompleteHidden: Boolean = false)

  val sidebar = ReactComponentB[Props]("Sidebar")
    .render({ P =>
      <.div(^.cls := "container-fluid",
        <.div(^.cls := "row",
          <.div(^.classSet1("col-sm-4 col-md-3 sidebar", ("sidebar-shown", P.isSidebarShown)),
            NotebookSelector(NotebookSelector.Props(P.b, P.itemMap, P.selectedNotebook, P.isAdding, P.isCompleteHidden))
          )
        )
      )
    }).build

  def apply(props: Props) = sidebar(props)
}
