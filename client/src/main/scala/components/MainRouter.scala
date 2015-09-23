package lodo

import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.prefix_<^._

trait AppLinks {
  def dashboard(content: TagMod*): ReactTag
}

object MainRouter extends RoutingRules {
  val dashboardLoc = register(rootLocation(Dashboard.dashboard))

  def appLinks(router: Router): AppLinks = new AppLinks {
    override def dashboard(content: TagMod*) = router.link(dashboardLoc)(content)
  }

  override protected val notFound = redirect(dashboardLoc, Redirect.Replace)

  // For now, do nothing with the intercept
  override protected def interceptRender(ic: InterceptionR) = ic.element
}
