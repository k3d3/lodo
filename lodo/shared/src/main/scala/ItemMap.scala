package lodo

import java.util.UUID

// If parent is None, item is at root
case class Item(id: UUID, parent: Option[UUID], timestamp: Long, contents: String) {
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
    case CompleteOp(item, itemChildren) =>
      ItemMap(items -- itemChildren.map(_.id) - item.id)
    case MoveOp(item, newParent) =>
      ItemMap(items + (item.id -> item.copy(parent = newParent)))
    case DuplicateOp(item, newId, newParent) =>
      ItemMap(items + (newId -> item.copy(id = newId, parent = newParent)))
  }

  def undo(op: Op): ItemMap = op match {
    case AddOp(item) =>
      ItemMap(items - item.id)
    case EditOp(item, _) =>
      ItemMap(items + (item.id -> item))
    case CompleteOp(item, itemChildren) =>
      ItemMap(items + (item.id -> item) ++ itemChildren.map(i => i.id -> i))
    case MoveOp(item, _) =>
      ItemMap(items + (item.id -> item))
    case DuplicateOp(_, newId, _) =>
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
case class CompleteOp(item: Item, itemChildren: Seq[Item]) extends Op
case class MoveOp(item: Item, newParent: Option[UUID]) extends Op
case class DuplicateOp(item: Item, newId: UUID, newParent: Option[UUID]) extends Op

sealed trait OpType
case class OpApply(op: Op) extends OpType
case class OpUndo(op: Op) extends OpType