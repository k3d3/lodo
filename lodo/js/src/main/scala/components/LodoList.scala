package lodo

import java.util.UUID

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.prefix_<^._
import eu.henkelmann.actuarius.ActuariusTransformer

import Helper._

object LodoList {
  case class Props(b: Dashboard.Backend, itemMap: ItemMap, item: Item, index: Int)

  case class State(isAdding: Boolean = false,isEditing: Boolean = false,
                   addText: String = "", editText: String,
                   isDragging: Boolean = false, isDragOver: Boolean = false)

  class Backend(t: BackendScope[Props, State]) {
    val mdTransformer = new ActuariusTransformer()
    def mdTransform(input: String): String = {
      // Ensure URLs are converted to links
      mdTransformer(
        input.replaceAll("(\\A|\\s|^)((http|https|ftp|mailto):\\S+)(\\s|\\z|$)", "$1[$2]($2)$4")
      ).toString
    }

    def onClickEdit(item: Item) =
      t.modState(s => {
        if (s.isEditing)
          t._props.b.applyOperation(EditOp(item, s.editText))
        s.copy(isEditing = !s.isEditing)
      })

    def onClickAdd(item: Item) =
      t.modState(s => s.copy(isAdding = !s.isAdding))

    def onEditChange(e: ReactEventI) =
      t.modState(s => s.copy(editText = e.currentTarget.value))

    def onAddChange(e: ReactEventI) =
      t.modState(s => s.copy(addText = e.currentTarget.value))

    def onFocus(e: ReactEventI) =
      e.currentTarget.select()

    def onEditSubmit(item: Item)(e: ReactEvent) = {
      e.preventDefault()
      t.modState(s => {
        t._props.b.applyOperation(EditOp(item, s.editText))
        s.copy(isEditing = false)
      })
    }

    def onAddSubmit(e: ReactEvent) = {
      e.preventDefault()
      t.modState(s => {
        t.props.b.applyOperation(AddOp(Item(UUID.randomUUID, Some(t.props.item.id), time(), s.addText)))
        s.copy(isAdding = false, addText = "")
      })
    }

    def onDragStart(e: ReactDragEvent) = {
      e.dataTransfer.effectAllowed = "move"
      e.dataTransfer.setData("lodo", t.props.item.id.toString)
      t.modState(_.copy(isDragging = true, isDragOver = false))
    }

    def onDragEnd(e: ReactDragEvent) =
      t.modState(_.copy(isDragging = false, isDragOver = false))

    def onDragEnter(e: ReactDragEvent) =
      t.modState(_.copy(isDragOver = true, isDragging = false))

    def onDragLeave(e: ReactDragEvent) =
      t.modState(_.copy(isDragOver = false, isDragging = false))

    def onDragOver(e: ReactDragEvent) = {
      t.modState(_.copy(isDragOver = true))
      e.stopPropagation()
      e.preventDefault()
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
        t.props.b.applyOperation(MoveOp(item, Some(dst)))
      })
    }
  }

  val list = ReactComponentB[Props]("List")
    .initialStateP(P => State(editText = P.item.contents))
    .backend(new Backend(_))
    .render((P, S, B) => {
      val children = P.itemMap.children(P.item.id)
      <.div(^.cls := "panel panel-default item",
        <.div(^.cls := (if (children.nonEmpty || S.isAdding) "panel-heading" else "panel-body"),
          ^.classSet(
            ("panel-heading", children.nonEmpty || S.isAdding),
            ("panel-body", !(children.nonEmpty || S.isAdding)),
            ("dragging", S.isDragging),
            ("dragover", S.isDragOver)
          ),
          ^.draggable := !S.isEditing,
          ^.onDragEnter ==> B.onDragEnter,
          ^.onDragLeave ==> B.onDragLeave,
          ^.onDragOver ==> B.onDragOver,
          ^.onDrop ==> B.onDrop,
          ^.onDragStart ==> B.onDragStart,
          ^.onDragEnd ==> B.onDragEnd,
          <.span(^.cls := "sel-num", P.index),
          <.span(^.cls := "content",
            if (S.isEditing)
              <.form(^.onSubmit ==> B.onEditSubmit(P.item),
                <.input(^.onFocus ==> B.onFocus, ^.autoFocus := true,
                  ^.defaultValue := P.item.contents, ^.onChange ==> B.onEditChange)
              )
            else
            <.span(
              ^.dangerouslySetInnerHtml(B.mdTransform(P.item.contents))
            )
          ),
          BtnGroup(
            BtnGroup.Props(P.item, "item",
              S.isEditing, S.isAdding,
              P.b.onClickComplete,
              B.onClickEdit,
              B.onClickAdd
            )
          )
        ),
        (children.nonEmpty || S.isAdding) ?= <.div(^.cls := "panel-body",
          children.nonEmpty ?= children
            .zipWithIndex
            .map { case (c, i) =>
              LodoList(LodoList.Props(P.b, P.itemMap, c, i))
            },
          S.isAdding ?= <.div(
            <.form(^.onSubmit ==> B.onAddSubmit,
              <.input(^.onFocus ==> B.onFocus, ^.autoFocus := true,
                ^.onChange ==> B.onAddChange)
            )
          )
        )
      )
    }).build

  def apply(props: Props): TagMod = list(props)
}
