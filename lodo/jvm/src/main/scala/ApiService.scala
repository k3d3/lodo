package lodo

import java.util.UUID

import lodo.Helper._

class ApiService extends LodoApi {
  object State {
    var items: ItemMap = new ItemMap(Seq(
      Item(testId(0), None, time(), "Notebook0"),
      Item(testId(1), None, time()+1, "Notebook1"),
      Item(testId(9), None, time()+2, "Notebook2"),

      Item(UUID.randomUUID, Some(testId(0)), time()+3, "N1Page1"),
      Item(testId(2), Some(testId(0)), time()+4, "N1Page2"),

      Item(UUID.randomUUID, Some(testId(2)), time()+5, "N1P2List1"),
      Item(testId(3), Some(testId(2)), time()+6, "N1P2List2"),
      Item(UUID.randomUUID, Some(testId(3)), time()+7, "N1P2L2Item1"),

      Item(UUID.randomUUID, Some(testId(2)), time()+8, "N1P2List3"),
      Item(UUID.randomUUID, Some(testId(2)), time()+9, "N1P2List4"),
      Item(UUID.randomUUID, Some(testId(2)), time()+10, "N1P2List5")
    ))
  }

  override def getItems(user: String) = State.items.items.map(_._2).toSeq

  override def applyOperation(op: Op): Boolean = {
    State.items = State.items(op)
    true
  }

  override def undoOperation(op: Op): Boolean = {
    State.items = State.items.undo(op)
    true
  }
}
