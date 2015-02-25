package lodo

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object Header {
  val component = ReactComponentB[Unit]("Header")
    .render(_ => {
    <.nav(^.className := "navbar navbar-default navbar-fixed-top",
      <.div(^.className := "container-fluid",
        <.div(^.className := "navbar-header",
          <.span(^.className := "navbar-brand", "Lodo")
        ),
        <.div(^.className := "collapse navbar-collapse")
      )
    )
  }).build
}
