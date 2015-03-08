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

import scala.util.Random

object ListColor {
  val defaultSeed: Long = 25

  val hslSaturation = 80
  val hslLightness = 94

  val minDifference = 60

  lazy val root: ListColor = ListColor(defaultSeed)
}

case class ListColor(seed: Long, lastHue: Option[Int] = None) {
  def next(): ListColor =
    ListColor(new Random(seed).nextLong(), Some(hue))

  val hue: Int = {
    val rng = new Random(seed)
    println(s"seed: $seed")
    Stream.continually(rng.nextInt(360)).take(100).find { item =>
      println(s"$item")
      lastHue == None ||
        (Math.abs((item-lastHue.head) % 360) > ListColor.minDifference &&
        Math.abs((lastHue.head-item) % 360) > ListColor.minDifference)
    }.getOrElse(0)
  }

  val hslString: String =
    f"hsl($hue, ${ListColor.hslSaturation}%%, ${ListColor.hslLightness}%%)"
}
