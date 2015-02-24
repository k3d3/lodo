package lodo

import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._

trait AppLinks {
  def dashboard(content: TagMod*): ReactTag
}

object MainRouter extends RoutingRules {
  val dashboardLoc = register(rootLocation(Dashboard.component))

  def appLinks(router: Router): AppLinks = new AppLinks {
    override def dashboard(content: TagMod*) = router.link(dashboardLoc)(content)
  }

  override protected val notFound = redirect(dashboardLoc, Redirect.Replace)

  override protected def interceptRender(ic: InterceptionR) = {
    <.div(
      <.nav(^.className := "navbar navbar-inverse navbar-fixed-top",
        <.div(^.className := "container",
          <.div(^.className := "navbar-header",
            <.span(^.className := "navbar-brand", "SPA Tutorial")
          ),
          <.div(^.className := "collapse navbar-collapse",
            "Hello"
          )
        )
      ),
      <.div(^.className := "container", ic.element)
    )
  }
}
