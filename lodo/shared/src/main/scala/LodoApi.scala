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

trait LodoApi {
  def getItems(user: String): (Seq[Item], Int, UUID)
  //def getCryptItems(user: String, sessId: String): (Seq[])

  def applyOperation(op: Op): Boolean
  //def applyCryptOperations(ops: Seq[CryptOp]): Boolean

  def redo(): Boolean
  def undo(): Boolean

  def getChanges(index: Int, sessId: UUID): Option[List[OpType]]
  //def getCryptChanges(index: Int, sessId: UUID): Option[List[CryptOp]]
}
