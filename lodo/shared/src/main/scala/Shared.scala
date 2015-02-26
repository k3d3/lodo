package lodo

import upickle._
import java.util.UUID

// All lists are considered "Items". The Notebooks are items, the pages within the notebooks are items,
// and every list within that are items. That way, you can move items freely amongst the tree. If you wanted,
// you could convert a deep list into a notebook by dragging it to the sidebar.

object Helper {
  implicit val uuid2Writer = upickle.Writer[UUID] {
    case t: UUID => Js.Str(t.toString)
  }
  implicit val uuid2Reader = upickle.Reader[UUID] {
    case Js.Str(s) => UUID.fromString(s)
  }

  def makeTestUUID(index: Int) = UUID.fromString(f"00000000-0000-0000-0000-$index%012d")
  final val testId = (0 until 10).map(makeTestUUID)
}

trait Api{
  def getItems(): Seq[Item]
}
