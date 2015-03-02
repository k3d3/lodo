package lodo

import org.squeryl.{Schema, SessionFactory, Session}
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.{PostgreSqlAdapter, H2Adapter}


object DbInterface {
  import Tables._
  Class.forName("org.h2.Driver")

  SessionFactory.concreteFactory = Some(()=>
    Session.create(
      java.sql.DriverManager.getConnection("jdbc:h2:~/lodo"),
      new H2Adapter))

  def getItems(): ItemMap = transaction {
    new ItemMap(items.toSeq)
  }

  def apply(op: Op) = transaction {
    op match {
      case op: AddOp =>
        items.insert(op.item)
      case op: EditOp =>
        update(items)(s =>
          where(s.id === op.item.id)
          set(s.contents := op.newContents)
        )
      case op: CompleteOp =>
        items.deleteWhere(_.id === op.item.id)
      case op: MoveOp =>
        update(items)(s =>
          where(s.id === op.item.id)
          set(s.parent := op.newParent)
        )
      case op: DuplicateOp => ()
    }
  }

  def undo(op: Op) = transaction {
    op match {
      case op: AddOp =>
        items.deleteWhere(_.id === op.item.id)
      case op: EditOp =>
        update(items)(s =>
          where(s.id === op.item.id)
          set(s.contents := op.item.contents)
        )
      case op: CompleteOp =>
        items.insert(op.item)
      case op: MoveOp =>
        update(items)(s =>
          where(s.id === op.item.id)
            set(s.parent := op.item.parent)
        )
      case op: DuplicateOp => ()
    }
  }
}

object Tables extends Schema {
  val items = table[Item]

  on(items)(s => declare(
    s.id is(unique, indexed),
    s.parent is indexed,
    s.timestamp is indexed
  ))
}