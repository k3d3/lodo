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

import java.util.UUID
import java.sql.Blob
import slick.driver.H2Driver.api._

object DbInterface {
  var items: Seq[Item] = Seq[Item]()

  def getItems(): ItemMap =
    new ItemMap(items)

  def apply(op: Op) = ???

  def undo(op: Op) = ???
}

/* We need a user that can own multiple notebooks
 * Each notebook can have zero or more items
 * Users can also share their owned notebooks with others
 * This ability to share will probably need pubkey crypto
 *
 * */

object DbTables {
  case class DbUser(id: UUID, email: String)

  class DbUsers(tag: Tag) extends Table[DbUser](tag, "Users") {
    def id = column[UUID]("id", O.PrimaryKey)
    def email = column[String]("email")
    def * = (id, email) <> (DbUser.tupled, DbUser.unapply)
  }

  val dbUsers = TableQuery[DbUsers]

  case class DbNotebook(id: UUID, ownerId: UUID)

  class DbNotebooks(tag: Tag) extends Table[DbNotebook](tag, "Notebooks") {
    def id = column[UUID]("id", O.PrimaryKey)
    def ownerId = column[UUID]("owner_id")
    def owner = foreignKey("owner_fk", ownerId, dbUsers)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def * = (id, ownerId) <> (DbNotebook.tupled, DbNotebook.unapply)
  }

  val dbNotebooks = TableQuery[DbNotebooks]

  case class DbItem(id: UUID, notebookId: UUID, revision: Int, data: Blob)

  class DbItems(tag: Tag) extends Table[DbItem](tag, "Items") {
    def id = column[UUID]("id", O.PrimaryKey)
    def notebookId = column[UUID]("notebook_id")
    def revision = column[Int]("revision")
    def data = column[Blob]("data")

    def notebook = foreignKey("notebook_fk", notebookId, dbNotebooks)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def * = (id, notebookId, revision, data) <> (DbItem.tupled, DbItem.unapply)
  }

  val dbItems = TableQuery[DbItems]
}

/*import org.squeryl.{Schema, SessionFactory, Session}
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
      case op: RemoveOp =>
        items.deleteWhere(_.id === op.item.id)
      case op: CompleteOp =>
        update(items)(s =>
          where(s.id === op.item.id)
            set(s.completed := op.completed)
        )
      case op: FoldOp =>
        update(items)(s =>
          where(s.id === op.item.id)
            set(s.folded := op.folded)
        )
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
      case op: RemoveOp =>
        items.insert(op.item)
      case op: CompleteOp =>
        update(items)(s =>
          where(s.id === op.item.id)
            set(s.completed := !op.completed)
        )
      case op: FoldOp =>
        update(items)(s =>
          where(s.id === op.item.id)
            set(s.folded := !op.folded)
        )
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
}*/
