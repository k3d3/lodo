The keyboard shortcuts are meant to be intuitive and fast. It may feel comfortable to someone who uses vim.
The idea is that there are two different sections of keyboard shortcuts: selectors and actions. There is also
a move/duplicate mode, in which two selectors are needed to perform an action.

# Selectors

Selectors are a means of navigating the tree of lists. They allow you to repeatedly select a specific
item in a list, move backwards, switch notebooks, or use the last selection. Typically, the root of a
notebook is selected by default, at which point you can navigate into the pages and lists.

* `1-9`: For any selected list, select a numbered internal list. Each list may turn into 00-99 if enough items exist.
* `L`: Use the selection that the last action was performed on (if archived
* `P`: Move back up one list, to the parent
* `H` (or `Esc` when not in Move/Duplicate Mode): Reset the selection to the root of the notebook
* `N`: Select the notebook root (can be used to select notebooks via N1-9, or create new notebooks)


# Global Actions

Global actions, as opposed to selection-based actions, are actions which are not performed on any selection.
Instead, they work on the whole state as one item.

* `U`: Undo the last performed action.
* `R`: Redo the last undone action.
* `F`: Find/filter item by text. (Look at the _Filtering_ section for more info)


# Selection-based Actions

Selection-based actions, as opposed to global actions, are performed once a selection has been made.
They can be used to add or edit items, move lists, or finish/archive lists or items.

* `A`: Add a new item to the currently selected list. To create a new notebook, `NA` can be used.
* `E`: Edit the text in an item.
* `C`: Complete and archive an item. Note that the "last selection" will then be the list containing the archived item.
* `M`: Move a list or item to a different place on the tree. Look at the Move/Duplicate Mode section for more info.
* `D`: Duplicate a list or item to a different location. Look at the _Move/Duplicate Mode_ section for more info.


# Move/Duplicate Mode

When a move or duplicate action is performed, a second selection is required as a destination.
By default, after pressing `M` or `D`, the current selector will be placed at the same location.
This means pressing `M, Enter` will make no changes, and `D, Enter` will copy the item and place
it in the same list. To be specific, the Move/Duplicate Mode is entered after pressing `M` or `D`.

* `Esc`: Leave Move/Duplicate Mode and return back to the root of the notebook
* `Enter`: Use current selection as destination


# Filtering

When the `F` key is pressed, the filter box will be highlighted and can be used to narrow down only the items and lists
containing a subset of what is filtered. Of course when filter box is in use, you cannot use any other actions, and
so this can be considered another mode. If there is text already in the filter box, you can use `F, Esc` to clear it.

* `Esc`: Clear any existing filter and return back to the root of the notebook
* `Enter`: Keep the filter and return back to the root of the notebook