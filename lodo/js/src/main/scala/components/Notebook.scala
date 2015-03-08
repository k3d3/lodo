package lodo

import java.util.UUID

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._

import Helper._

object Notebook {
  case class Props(b: Dashboard.Backend, selectedNotebook: Option[UUID],
                   isAdding: Boolean, itemMap: ItemMap, item: Item, index: Int)

  case class State(isEditing: Boolean = false,
                   addText: String = "", editText: String,
                   isDragging: Boolean = false, isDragOver: Boolean = false)

  class Backend(t: BackendScope[Props, State]) {
    def onClickEdit(item: Item) = {
      t.modState(s => {
        if (s.isEditing)
          t.props.b.applyOperation(EditOp(item, s.editText))
        s.copy(isEditing = !s.isEditing)
      })
    }

    def onClickAdd(item: Item) = {
      t.props.b.onNotebookClickAdd(item)
    }

    def onEdit(e: ReactEventI) =
      t.modState(s => s.copy(editText = e.currentTarget.value))

    def onFocus(e: ReactEventI) =
      e.currentTarget.select()

    def onSubmit(item: Item)(e: ReactEvent) = {
      e.stopPropagation()
      e.preventDefault()

      t.modState(s => {
        t.props.b.applyOperation(EditOp(item, s.editText))
        s.copy(isEditing = false)
      })
    }

    def onDragStart(e: ReactDragEvent) = {
      e.stopPropagation()

      e.dataTransfer.effectAllowed = "move"
      e.dataTransfer.setData("lodo", t.props.item.id.toString)
      t.modState(_.copy(isDragging = true, isDragOver = false))
    }

    def onDragEnd(e: ReactDragEvent) = {
      e.stopPropagation()

      t.modState(_.copy(isDragging = false, isDragOver = false))
    }

    def onDragEnter(e: ReactDragEvent) = {
      e.stopPropagation()
      //e.preventDefault()

      t.modState(_.copy(isDragOver = true, isDragging = false))
      t.props.b.selectNotebook(t.props.item)
    }

    def onDragLeave(e: ReactDragEvent) = {
      e.stopPropagation()
      //e.preventDefault()

      t.modState(_.copy(isDragOver = false, isDragging = false))
    }

    def onDragOver(e: ReactDragEvent) = {
      e.stopPropagation()
      e.preventDefault()
      t.modState(_.copy(isDragOver = true))
    }

    def onDrop(e: ReactDragEvent): Unit = {
      e.stopPropagation()
      e.preventDefault()
      t.modState(_.copy(isDragOver = false, isDragging = false))

      val src = UUID.fromString(e.dataTransfer.getData("lodo"))
      val dst = t.props.item.id

      if (src == dst || t.props.itemMap.isChild(src, dst))
        return // Don't allow drop on self or child

      t.props.itemMap(Some(src)).map(item => {
        val op = MoveOp(item, Some(dst), time())
        if (t.props.selectedNotebook == Some(src))
          t.props.b.moveAndSelectNotebook(op, dst)
        else
          t.props.b.applyOperation(op)
      })
    }
  }

  val notebook = ReactComponentB[Props]("Notebook")
    .initialStateP(P => State(editText = P.item.contents))
    .backend(new Backend(_))
    .render((P, S, B) => {
      <.li(^.key := P.item.id.toString,
        ^.draggable := !S.isEditing,
        ^.onDragEnter ==> B.onDragEnter,
        ^.onDragLeave ==> B.onDragLeave,
        ^.onDragOver ==> B.onDragOver,
        ^.onDrop ==> B.onDrop,
        ^.classSet(
          ("active", P.selectedNotebook == Some(P.item.id)),
          ("dragging", S.isDragging),
          ("dragover", S.isDragOver)
        ),
        <.a(^.href := "#",
          ^.onClick --> P.b.selectNotebook(P.item),
          ^.onDragStart ==> B.onDragStart,
          ^.onDragEnd ==> B.onDragEnd,
          <.span(^.cls := "sel-num", P.index),
          <.span(^.cls := "content",
            if (S.isEditing)
              <.form(^.onSubmit ==> B.onSubmit(P.item),
                <.input(^.onFocus ==> B.onFocus, ^.autoFocus := true,
                  ^.defaultValue := P.item.contents, ^.onChange ==> B.onEdit)
              )
            else
              <.span(^.cls := "content-data", P.item.contents)
          ),
          BtnGroup(
            BtnGroup.Props(P.item, "page",
              S.isEditing, if (P.selectedNotebook == Some(P.item.id)) P.isAdding else false,
              P.b.onClickComplete,
              B.onClickEdit,
              B.onClickAdd
            )
          )
        )
      )
    }).build


  def apply(props: Props) = notebook(props)
}
