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

import scala.scalajs.js
import scala.scalajs.js.typedarray
import scala.scalajs.js.annotation.JSName


@JSName("sodium")
@js.native
object LibSodium extends js.Object {
  @JSName("crypto_box_keypair")
  def cryptoBoxKeypair(): BoxKeypair = js.native
}

@js.native
trait BoxKeypair extends js.Object {
  val publicKey: typedarray.Uint8Array = js.native
  val privateKey: typedarray.Uint8Array = js.native
  val keyType: String = js.native
}
