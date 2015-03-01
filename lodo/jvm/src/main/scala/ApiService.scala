package lodo

import java.util.UUID
import akka.actor._
import akka.event.{ActorEventBus, LookupClassification, EventBus}
import akka.util.Timeout

import lodo.Helper._

import scala.concurrent.{Await, Promise, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class ApiService(val system: ActorSystem) extends LodoApi {
  case class LastOps(ops: List[OpType], lastIndex: Int, limit: Int = 100) {
    def addOp(op: OpType): LastOps =
      LastOps((op :: ops).take(100), lastIndex + 1)

    def getOpsSince(index: Int): Option[List[OpType]] = {
      println(s"getOpsSince index: $index, lastIndex: $lastIndex, i-lI: ${lastIndex-index}")
      if ((lastIndex-index > 100) || index > lastIndex) {
        println("Noned")
        None
      }
      else
        Some(ops.take(lastIndex-index))
    }

    def waitForNewOp(index: Int): Boolean =
      index == lastIndex
  }

  class OpBus extends ActorEventBus with LookupClassification {
    type Event = OpType
    type Classifier = Unit

    protected def mapSize(): Int =
      10

    protected def classify(event: Event): Classifier =
      ()

    protected def publish(event: Event, subscriber: Subscriber): Unit = {
      subscriber ! event
    }
  }

  val opBus = new OpBus

  object State {
    /*var itemMap1: ItemMap = new ItemMap(Seq(
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
    ))*/

    var itemMap: ItemMap = DbInterface.getItems()

    val stackLimit = 100
    var undoStack: List[Op] = List()
    var redoStack: List[Op] = List()

    var lastOps = LastOps(List(), 0)
  }

  override def getItems(user: String): (Seq[Item], Int) = {
    println("api getItems")
    (State.itemMap.items.map{ case (_, item) => item }.toSeq, State.lastOps.lastIndex)
  }


  override def applyOperation(op: Op): Boolean = {
    println("api applyOperation")
    State.itemMap = State.itemMap(op)
    opBus.publish(OpApply(op))
    State.lastOps = State.lastOps.addOp(OpApply(op))
    State.undoStack = (op :: State.undoStack).take(State.stackLimit)
    State.redoStack = List()
    DbInterface.apply(op)
    true
  }

  override def undo(): Boolean = {
    println("api undo")
    if (State.undoStack.nonEmpty) {
      State.itemMap = State.itemMap.undo(State.undoStack.head)
      opBus.publish(OpUndo(State.undoStack.head))
      DbInterface.undo(State.undoStack.head)
      State.lastOps = State.lastOps.addOp(OpUndo(State.undoStack.head))
      State.redoStack = (State.undoStack.head :: State.redoStack).take(State.stackLimit)
      State.undoStack = State.undoStack.tail
    }
    true
  }

  override def redo(): Boolean = {
    println("api redo")
    if (State.redoStack.nonEmpty) {
      State.itemMap = State.itemMap(State.redoStack.head)
      opBus.publish(OpApply(State.redoStack.head))
      DbInterface.undo(State.redoStack.head)
      State.lastOps = State.lastOps.addOp(OpApply(State.redoStack.head))
      State.undoStack = (State.redoStack.head :: State.undoStack).take(State.stackLimit)
      State.redoStack = State.redoStack.tail
    }
    true
  }

  override def getChanges(lastOp: Int): Option[List[OpType]] = {
    println("api getChanges")
    if (State.lastOps.waitForNewOp(lastOp)) {
      val promise = Promise[OpType]()
      val listener = system.actorOf(Props(new Actor {
        def receive = {
          case o: OpType =>
            promise.trySuccess(o)
        }
      }))
      opBus.subscribe(listener, ())
      val result = Try(Some(List(Await.result(promise.future, Timeout(5.seconds).duration))))
        .getOrElse(Some(List()))
      opBus.unsubscribe(listener, ())
      result
    }
    else {
      println("noWait")
      val results =State.lastOps.getOpsSince(lastOp)
      println(s"ops result: $results")
      results
    }
  }
}
