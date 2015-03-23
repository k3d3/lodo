package lodo

import japgolly.scalajs.react.ReactComponentB
import japgolly.scalajs.react.vdom.prefix_<^._

object Header {
  case class Props(b: Dashboard.Backend, isShowSidebar: Boolean, isHideComplete: Boolean, isQuickAdd: Boolean)

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
            <.button(
              ^.classSet1("navbar-toggle collapsed", ("nav-selected", P.isShowSidebar)),
              ^.tpe := "button",
              ^.onClick --> P.b.toggleShowSidebar(),
              <.span(^.cls := "glyphicon glyphicon-book")
            ),
            <.button(
              ^.classSet1("navbar-toggle collapsed", ("nav-selected", !P.isHideComplete)),
              ^.tpe := "button",
              ^.onClick --> P.b.toggleCompleted(),
              <.span(^.cls := "glyphicon glyphicon-ok-circle")
            ),
            <.button(
              ^.classSet1("navbar-toggle collapsed", ("nav-selected", P.isQuickAdd)),
              ^.tpe := "button",
              ^.onClick --> P.b.toggleQuickAdd(),
              <.span(^.cls := "glyphicon glyphicon-time")
            ),
            <.span(^.cls := "navbar-brand",
              <.span(^.cls := "glyphicon glyphicon-check"),
              "Lodo"
            )
          ),
          <.div(^.cls := "navbar-collapse collapse",
            <.p(^.cls := "navbar-text", "Toggle:"),
            <.ul(^.cls := "nav navbar-nav",
              <.li(
                ^.classSet(("nav-selected", !P.isShowSidebar)),
                <.a(^.href := "#",
                ^.onClick --> P.b.toggleShowSidebar(), "Notebooks")
              ),
              <.li(
                ^.classSet(("nav-selected", !P.isHideComplete)),
                <.a(^.href := "#",
                ^.onClick --> P.b.toggleCompleted(), "Completed")
              ),
              <.li(
                ^.classSet(("nav-selected", P.isQuickAdd)),
                <.a(^.href := "#",
                ^.onClick --> P.b.toggleQuickAdd(), "Quick Add")
              )
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
