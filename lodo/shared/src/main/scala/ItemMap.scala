package lodo

import java.util.UUID

// If parent is None, item is at root
case class Item(id: UUID, parent: Option[UUID], timestamp: Int, contents: String)

case class ItemMap(items: Map[UUID, Item] = Map[UUID, Item]()) {
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
      .filter(_._2.parent == None)
      .map(_._2)
      .toSeq
      .sortBy(_.timestamp)

  def children(itemId: UUID): Seq[Item] =
    items
      .filter(_._2.parent == Some(itemId))
      .map(_._2)
      .toSeq
      .sortBy(_.timestamp)

  def recursiveChildren(itemId: UUID): Seq[Item] = {
    val childItems = items
      .filter(_._2.parent == Some(itemId))
      .map(_._2)

    childItems
      .flatMap(i => recursiveChildren(i.id))
      .toSeq ++ childItems
  }
}

// We need to ensure there's enough information to reverse operations
sealed trait Op
case class AddOp(item: Item) extends Op
case class EditOp(item: Item, newContents: String) extends Op
case class CompleteOp(item: Item, itemChildren: Seq[Item]) extends Op
case class MoveOp(item: Item, newParent: Option[UUID]) extends Op
case class DuplicateOp(item: Item, newId: UUID, newParent: Option[UUID]) extends Op