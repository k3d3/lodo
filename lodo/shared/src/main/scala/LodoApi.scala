package lodo

trait LodoApi {
  def getItems(user: String): (Seq[Item], Int)

  def applyOperation(op: Op): Boolean
  def redo(): Boolean
  def undo(): Boolean

  def getChanges(index: Int): Option[List[LastOp]]
}
