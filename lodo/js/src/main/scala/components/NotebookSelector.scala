package components

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object NotebookSelector {
  val component = ReactComponentB[Seq[String]]("NotebookSelector")
    .render(notebooks => {
      <.div("hi")
  })
}
