package lodo

import java.lang.RuntimeException

import org.squeryl.{Schema, SessionFactory, Session}
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.H2Adapter


object DbInterface {
  import Tables._
  Class.forName("org.h2.Driver")

  val devMode = System.getProperty("DEVMODE", "false").toBoolean

  val connString =
    if (devMode)
      "jdbc:h2:lodo"
    else
      "jdbc:h2:lodo;TRACE_LEVEL_FILE=0"

  SessionFactory.concreteFactory = Some(()=>
    Session.create(
      java.sql.DriverManager.getConnection(connString),
      new H2Adapter))

  transaction {
    try {
      Tables.create
      println("DB doesn't exist - tables created")
    } catch {
      case e: RuntimeException => println("DB exists - no tables created")
    }
  }

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