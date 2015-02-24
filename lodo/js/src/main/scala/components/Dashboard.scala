package lodo

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object Dashboard {
  val component = ReactComponentB[MainRouter.Router]("Dashboard")
    .render(router => {
      val appLinks = MainRouter.appLinks(router)
      <.div(
        <.h2("hello"),
        "This is cake."
      )
    }).build
}
