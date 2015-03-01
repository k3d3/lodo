package lodo

import akka.actor._
import akka.event.{ActorEventBus, LookupClassification}
import akka.util.Timeout

import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class ApiService(val system: ActorSystem) extends LodoApi {
  case class LastOps(ops: List[OpType], lastIndex: Int, limit: Int = 100) {
    def addOp(op: OpType): LastOps =
      LastOps((op :: ops).take(100), lastIndex + 1)

    def getOpsSince(index: Int): Option[List[OpType]] =
      if ((lastIndex-index > 100) || index > lastIndex)
        None
      else
        Some(ops.take(lastIndex-index))

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
    var itemMap: ItemMap = DbInterface.getItems()

    val stackLimit = 100
    var undoStack: List[Op] = List()
    var redoStack: List[Op] = List()

    var lastOps = LastOps(List(), 0)
  }

  override def getItems(user: String): (Seq[Item], Int) =
    (State.itemMap.items.map{ case (_, item) => item }.toSeq, State.lastOps.lastIndex)


  override def applyOperation(op: Op): Boolean = {
    State.itemMap = State.itemMap(op)
    opBus.publish(OpApply(op))
    State.lastOps = State.lastOps.addOp(OpApply(op))
    State.undoStack = (op :: State.undoStack).take(State.stackLimit)
    State.redoStack = List()
    DbInterface.apply(op)
    true
  }

  override def undo(): Boolean = {
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
    else
      State.lastOps.getOpsSince(lastOp)
  }
}
