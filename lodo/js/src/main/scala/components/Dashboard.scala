package lodo

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object Dashboard {
  val component = ReactComponentB[MainRouter.Router]("Dashboard")
    .render(router => {
      val appLinks = MainRouter.appLinks(router)
      <.div(
        <.nav(^.className := "navbar navbar-default navbar-fixed-top",
          <.div(^.className := "container-fluid",
            <.div(^.className := "navbar-header",
              <.span(^.className := "navbar-brand", "Lodo")
            ),
            <.div(^.className := "collapse navbar-collapse")
          )
        ),
        <.div(^.className := "container")
      )
    }).build
}
