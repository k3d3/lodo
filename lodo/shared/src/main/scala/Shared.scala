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

  def time(): Long = System.currentTimeMillis()

  def columnize[A](items: Seq[A], count: Int): Seq[Seq[A]] = {
    import scala.collection.mutable.Buffer
    items
      .zipWithIndex
      .foldLeft(Seq.fill(count)(Buffer[A]())){ case (lists, (item, index)) =>
        lists(index % count) += item
        lists
      }
      .map(_.toSeq)
  }
}

trait Api{
  def getItems(): Seq[Item]
}
