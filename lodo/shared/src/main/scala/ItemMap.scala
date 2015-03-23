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

// If parent is None, item is at root
case class Item(id: UUID, parent: Option[UUID], timestamp: Long, contents: String,
                completed: Boolean = false, folded: Boolean = false) {
  def this() = this(new UUID(0, 0), None, 0, "")
}

case class ItemMap(items: Map[UUID, Item] = Map()) {
  def this(items: Seq[Item]) = this(items.map(i => i.id -> i).toMap)
  def apply(id: Option[UUID]): Option[Item] = id.flatMap(items.lift)

  def apply(op: Op): ItemMap = op match {
    case AddOp(item) =>
      ItemMap(items + (item.id -> item))
    case EditOp(item, newContents) =>
      ItemMap(items + (item.id -> item.copy(contents = newContents)))
    case RemoveOp(item, itemChildren) =>
      ItemMap(items -- itemChildren.map(_.id) - item.id)
    case CompleteOp(item, completed) =>
      ItemMap(items + (item.id -> item.copy(completed = completed)))
    case MoveOp(item, newParent, newTimestamp) =>
      ItemMap(items + (item.id -> item.copy(parent = newParent, timestamp = newTimestamp)))
    case DuplicateOp(item, newId, newParent, newTimestamp) =>
      ItemMap(items + (newId -> item.copy(id = newId, parent = newParent, timestamp = newTimestamp)))
  }

  def undo(op: Op): ItemMap = op match {
    case AddOp(item) =>
      ItemMap(items - item.id)
    case EditOp(item, _) =>
      ItemMap(items + (item.id -> item))
    case RemoveOp(item, itemChildren) =>
      ItemMap(items + (item.id -> item) ++ itemChildren.map(i => i.id -> i))
    case CompleteOp(item, completed) =>
      ItemMap(items + (item.id -> item.copy(completed = !completed)))
    case MoveOp(item, _, _) =>
      ItemMap(items + (item.id -> item))
    case DuplicateOp(_, newId, _, _) =>
      ItemMap(items - newId)
  }

  def notebooks(): Seq[Item] =
    items
      .filter{ case (_, item) => item.parent == None }
      .map{ case (_, item) => item }
      .toSeq
      .sortBy(_.timestamp)

  def children(itemId: UUID): Seq[Item] =
    items
      .filter{ case (_, item) => item.parent == Some(itemId) }
      .map{ case (_, item) => item }
      .toSeq
      .sortBy(_.timestamp)

  def recursiveChildren(itemId: UUID): Seq[Item] = {
    val childItems = items
      .filter{ case (_, item) => item.parent == Some(itemId) }
      .map{ case (_, item) => item }

    childItems
      .flatMap(i => recursiveChildren(i.id))
      .toSeq ++ childItems
  }

  def isChild(itemId: UUID, childId: UUID): Boolean =
    recursiveChildren(itemId).filter(_.id == childId).nonEmpty

  def getNotebook(itemId: UUID): Option[Item] =
    apply(Some(itemId)).flatMap{item =>
      if (item.parent == None)
        Some(item)
      else
        getNotebook(item.id)
    }
}

// We need to ensure there's enough information to reverse operations
sealed trait Op
case class AddOp(item: Item) extends Op
case class EditOp(item: Item, newContents: String) extends Op
case class RemoveOp(item: Item, itemChildren: Seq[Item]) extends Op
case class CompleteOp(item: Item, completed: Boolean) extends Op
case class MoveOp(item: Item, newParent: Option[UUID], newTimestamp: Long) extends Op
case class DuplicateOp(item: Item, newId: UUID, newParent: Option[UUID], newTimestamp: Long) extends Op

sealed trait OpType
case class OpApply(op: Op) extends OpType
case class OpUndo(op: Op) extends OpType

/*
* What is needed for client-side encryption?
*
* Server side:
* - Encrypted data associated with unencrypted ID
*   - Could this just be a tuple of UUID and EncryptedData?
* - Simple operations that can be performed on encrypted data
*   - AddOp
*     - Contains new unencrypted UUID and new encrypted data
*     - To reverse, delete data associated with ID
*   - ReplaceOp
*     - Contains existing unencrypted UUID and new encrypted data
*     - To reverse, keep track of old data, and replace new with old
*   - DeleteOp
*     - Contains existing unencrypted UUID
*     - To reverse, keep track of old data, and add old data
*
* Client side:
* - Encrypted data associated with unencrypted ID (just for communications)
*   - Decrypted ID and data (which could just be an Item)
*
* With encrypted data, and undo being tracked by the simple operations rather than the complicated operations,
* do the complex operations need to exist anymore? They'll never be applied to the itemMap with encryption.
* They can however be useful to convert complicated operations into simple operations.
**/

case class CryptItem(iv: Array[Byte], data: Array[Byte]) {
  def toItem(id: UUID, key: Array[Byte]): Item = {
    Item(id, None, 0, "")
  }
}

case class CryptItemMap(items: Map[UUID,CryptItem]) {
  def apply(op: CryptOp): CryptItemMap = ???
  def applyWithUndo(op: CryptOp): (CryptItemMap, CryptOp) = ???

  def toItemMap(key: Array[Byte]): ItemMap = ???
}

sealed trait CryptOp
case class CryptAddOp(id: UUID, data: CryptItem)