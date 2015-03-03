package lodo

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object Header {
  case class Props(b: Dashboard.Backend)

  val header = ReactComponentB[Props]("Header")
    .render(P => {
      <.nav(^.cls := "navbar navbar-default navbar-fixed-top",
        <.div(^.cls := "container-fluid",
          <.div(^.cls := "navbar-header",
            <.button(^.cls := "navbar-toggle collapsed", ^.tpe := "button",
              ^.onClick ==> P.b.performRedo(),
              <.span(^.cls := "glyphicon glyphicon-forward")
            ),
            <.button(^.cls := "navbar-toggle collapsed", ^.tpe := "button",
              ^.onClick ==> P.b.performUndo(),
              <.span(^.cls := "glyphicon glyphicon-backward")
            ),
            <.button(^.cls := "navbar-toggle collapsed", ^.tpe := "button",
              <.span(^.cls := "glyphicon glyphicon-search")
            ),
            <.button(^.cls := "navbar-toggle collapsed", ^.tpe := "button",
              ^.onClick --> P.b.toggleShowSidebar(),
              <.span(^.cls := "glyphicon glyphicon-book")
            ),
            <.span(^.cls := "navbar-brand",
              <.span(^.cls := "glyphicon glyphicon-check"),
              "Lodo"
            )
          ),
          <.div(^.cls := "navbar-collapse collapse",
            <.ul(^.cls := "nav navbar-nav",
              <.li(<.a(^.href := "#",
                ^.onClick --> P.b.toggleShowSidebar(), "Toggle Notebooks"))
            ),
            <.ul(^.cls := "nav navbar-nav navbar-right",

              <.li(<.a(^.href := "#",
                ^.onClick ==> P.b.performUndo(), "Undo")),
              <.li(<.a(^.href := "#",
                ^.onClick ==> P.b.performRedo(), "Redo"))
            ),
          <.form(^.cls := "navbar-form navbar-right",
            <.input(^.cls := "form-control filter",
                    ^.tpe := "text",
                    ^.placeholder := "\uE003"))
          )
        )
      )
    }).build

  def apply(props: Props) = header(props)

}
