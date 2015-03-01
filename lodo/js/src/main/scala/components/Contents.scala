package lodo

import java.util.UUID

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import lodo.Helper._

object Contents {
  case class Props(b: Dashboard.Backend, itemMap: ItemMap, selectedNotebook: Option[UUID],
                   isAdding: Boolean, isSidebarShown: Boolean)

  case class State(addText: String = "")

  class Backend(t: BackendScope[Props, State]) {
    def onFocus(e: ReactEventI) =
      e.currentTarget.select()

    def onAddChange(e: ReactEventI) =
      t.modState(s => s.copy(addText = e.currentTarget.value))

    def onAddSubmit(e: ReactEvent) = {
      e.preventDefault()
      t.modState(s => {
        t.props.b.onAddComplete(AddOp(Item(UUID.randomUUID, t.props.selectedNotebook, time(), s.addText)))
        s.copy(addText = "")
      })
    }
  }

  val contents = ReactComponentB[Props]("Contents")
    .initialState(State())
    .backend(new Backend(_))
    .render((P, S, B) => {
      <.div(^.id := "lodo-contents",
        ^.classSet1("main",
          ("col-sm-8 col-sm-offset-4 col-md-9 col-md-offset-3", P.isSidebarShown),
          ("col-sm-12 col-md-12", !P.isSidebarShown)
        ),
        P.selectedNotebook.map(n =>
          P.itemMap
            .children(n)
            .zipWithIndex
            .map{ case (p, i) => Page(Page.Props(P.b, P.itemMap, p, i, P.isSidebarShown)) }
        ),
        P.isAdding ?= <.div(
          <.a(^.href := "#",
            <.form(^.onSubmit ==> B.onAddSubmit,
              <.input(^.onFocus ==> B.onFocus, ^.autoFocus := true,
                ^.onChange ==> B.onAddChange)
            )
          )
        )
      )
    }).build

  def apply(props: Props) = contents(props)
}
