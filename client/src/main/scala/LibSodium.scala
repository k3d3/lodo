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
import scala.scalajs.js.typedarray.Uint8Array
import scala.scalajs.js.annotation.JSName

final object types {
  type BoxPublicKey = Uint8Array
  type BoxPrivateKey = Uint8Array

  type Key = Uint8Array
  type Nonce = Uint8Array
}

import types._

@js.native
class TextEncoder extends js.Object {
  def encode(str: String): Uint8Array = js.native
}

@js.native
class TextDecoder extends js.Object {
  def decode(data: Uint8Array): String = js.native
}

object Utf8Codec {
  lazy val encoder = new TextEncoder
  lazy val decoder = new TextDecoder

  def encode = encoder.encode _
  def decode = decoder.decode _
}

object Key {
  def fromPassword(password: String, salt: String = "",
                   opsLimit: Int = 16, memLimit: Int = 16): Key =
    Utf8Codec.encode("testing")
  def fromData(data: Uint8Array): Key =
    // TODO: return zero-padded array if less than size,
    // or 
    Utf8Codec.encode("testing")
}

@JSName("sodium")
@js.native
object LibSodium extends js.Object {
  // Public key stuff
  @JSName("crypto_box_keypair")
  def cryptoBoxKeypair(): BoxKeypair = js.native
  @JSName("crypto_box_seal")
  def cryptoBoxSeal(message: Uint8Array, publicKey: BoxPublicKey): Uint8Array = js.native
  @JSName("crypto_box_seal_open")
  def cryptoBoxSealOpen(ciphertext: Uint8Array, publicKey: BoxPublicKey, privateKey: BoxPrivateKey): Uint8Array = js.native

  // Password Hashing
  @JSName("crypto_pwhash_scryptsalsa208sha256")
  def cryptoPwhashScrypt(password: Uint8Array, salt: Uint8Array, opsLimit: Int, memLimit: Int, keyLength: Int) = js.native

  // Generic SHA2 hashing

  // Symmetric encryption
}

@js.native
trait BoxKeypair extends js.Object {
  val publicKey: BoxPublicKey = js.native
  val privateKey: BoxPrivateKey = js.native
  val keyType: String = js.native
}
