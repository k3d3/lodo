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
  case class LastOps(ops: List[LastOp], lastIndex: Int, limit: Int = 100) {
    def addOp(op: Op, opType: OpType): LastOps =
      LastOps((LastOp(op, opType) :: ops).take(100), lastIndex + 1)

    def getOpsSince(index: Int): Option[List[LastOp]] =
      if (lastIndex-index >= 100 || index > lastIndex)
        None
      else
        Some(ops.take(lastIndex-index))

    def waitForNewOp(index: Int): Boolean =
      index == lastIndex
  }

  class OpBus extends ActorEventBus with LookupClassification {
    type Event = LastOp
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

    var lastOps = LastOps(List(), 0)
  }

  override def getItems(user: String): (Seq[Item], Int) =
    (State.items.items.map(_._2).toSeq, State.lastOps.lastIndex)

  override def applyOperation(op: Op): Boolean = {
    State.items = State.items(op)
    State.lastOps = State.lastOps.addOp(op, OpApply)
    println("apply")
    opBus.publish(LastOp(op, OpApply))
    println("after apply")
    true
  }

  override def undoOperation(op: Op): Boolean = {
    State.items = State.items.undo(op)
    State.lastOps = State.lastOps.addOp(op, OpUndo)

    opBus.publish(LastOp(op, OpUndo))
    true
  }

  override def getChanges(lastOp: Int): Option[List[LastOp]] = {
    if (State.lastOps.waitForNewOp(lastOp)) {
      val promise = Promise[LastOp]()
      val listener = system.actorOf(Props(new Actor {
        def receive = {
          case o: LastOp => {
            println(s"got message $o")
            promise.trySuccess(o)
          }
        }
      }))
      println("subscribe")
      opBus.subscribe(listener, ())
      println("waiting")
      val result = Try(Some(List(Await.result(promise.future, Timeout(5.seconds).duration))))
        .getOrElse(Some(List()))
      opBus.unsubscribe(listener, ())
      result
    }
    else
      State.lastOps.getOpsSince(lastOp)
  }
}
