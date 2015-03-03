package lodo

import java.util.UUID

trait LodoApi {
  def getItems(user: String): (Seq[Item], Int, UUID)

  def applyOperation(op: Op): Boolean
  def redo(): Boolean
  def undo(): Boolean

  def getChanges(index: Int, sessId: UUID): Option[List[OpType]]
}
