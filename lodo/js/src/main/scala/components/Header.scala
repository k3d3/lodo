package lodo

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object Header {
  val header = ReactComponentB[Unit]("Header")
    .render(_ => {
      <.nav(^.cls := "navbar navbar-default navbar-fixed-top",
        <.div(^.cls := "container-fluid",
          <.div(^.cls := "navbar-header",
            <.button(^.cls := "navbar-toggle collapsed", ^.tpe := "button",
              "data-toggle".reactAttr := "collapse",
              "data-target".reactAttr := "#navbar",
              <.span(^.cls := "glyphicon glyphicon-menu-hamburger")
            ),
            <.button(^.cls := "navbar-toggle collapsed", ^.tpe := "button",
              <.span(^.cls := "glyphicon glyphicon-book")
            ),
            <.span(^.cls := "navbar-brand",
              <.span(^.cls := "glyphicon glyphicon-check"),
              "Lodo"
            )
          ),
          <.div(^.cls := "collapse navbar-collapse",
            <.ul(^.cls := "nav navbar-nav navbar-right",
              <.li(<.a(^.href := "#", "Undo")),
              <.li(<.a(^.href := "#", "Redo"))
            ),
          <.form(^.cls := "navbar-form navbar-right",
            <.input(^.cls := "form-control filter",
                    ^.tpe := "text",
                    ^.placeholder := "\uE003"))
          )
        )
      )
    }).buildU

  def apply() = header()

}
