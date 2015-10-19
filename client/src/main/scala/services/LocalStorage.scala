package lodo

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSName

@JSName("localStorage")
@js.native
object LocalStorage extends js.Object {
  def getItem(key: String): UndefOr[String] = js.native
  def setItem(key: String, value: String): Unit = js.native
}
