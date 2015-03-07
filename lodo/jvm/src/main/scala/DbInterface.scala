/*
Lodo is a layered to-do list (Outliner)
Copyright (C) 2015 Keith Morrow.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License v3 as
published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package lodo

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