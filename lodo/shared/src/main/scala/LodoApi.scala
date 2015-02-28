package lodo

trait LodoApi {
  def getItems(user: String): Seq[Item]

  def applyOperation(op: Op): Boolean

  def undoOperation(op: Op): Boolean
}
