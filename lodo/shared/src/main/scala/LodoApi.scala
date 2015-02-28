package lodo

trait LodoApi {
  def getItems(user: String): Seq[Item]

  def applyOperation(op: Op): Unit

  def undoOperation(op: Op): Unit
}
