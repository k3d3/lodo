package lodo

trait LodoApi {
  def getItems(user: String): (Seq[Item], Int)

  def applyOperation(op: Op): Boolean

  def undoOperation(op: Op): Boolean

  def getChanges(index: Int): Option[List[LastOp]]
}
