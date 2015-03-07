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

import upickle._
import java.util.UUID

// All lists are considered "Items". The Notebooks are items, the pages within the notebooks are items,
// and every list within that are items. That way, you can move items freely amongst the tree. If you wanted,
// you could convert a deep list into a notebook by dragging it to the sidebar.

object Helper {
  implicit val uuid2Writer = upickle.Writer[UUID] {
    case t: UUID => Js.Str(t.toString)
  }
  implicit val uuid2Reader = upickle.Reader[UUID] {
    case Js.Str(s) => UUID.fromString(s)
  }

  def time(): Long = System.currentTimeMillis()
}

trait Api{
  def getItems(): Seq[Item]
}
