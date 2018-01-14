package com.greg.ui.action

import com.greg.ui.action.change.Change
import com.greg.ui.action.change.ChangeType
import com.greg.ui.action.containers.ActionList
import com.greg.ui.action.containers.Actions
import com.greg.ui.canvas.Canvas
import com.greg.ui.canvas.widget.Widgets
import com.greg.ui.canvas.widget.builder.WidgetMementoBuilderAdapter
import com.greg.ui.canvas.widget.memento.mementoes.Memento
import com.greg.ui.canvas.widget.type.types.WidgetGroup
import tornadofx.move

class ActionManager(private val widgets: Widgets, private val canvas: Canvas) {

    private val actions = Actions()
    private val redo = ActionList()
    private var cached: WidgetGroup? = null
    private var ignore = false

    fun start(widget: WidgetGroup? = null) {
        actions.start()
        //Must remove ignore before first record
        ignore = false
        if(widget != null)
            record(ChangeType.CHANGE, widget)
    }

    fun finish() {
        if(actions.finish())
            redo.clear()
    }

    fun record(type: ChangeType, widget: WidgetGroup) {
        record(type, widget.identifier, widget.getMemento())
    }

    fun record(type: ChangeType, identifier: Int, value: Any) {
        if (!ignore) {
            val change = Change(type, identifier, value)
            actions.record(change)
        }
    }

    @Suppress("LoopToCallChain")
    fun undo() {
        if (actions.isNotEmpty()) {
            ignore = true
            val last = actions.last()
            for (change in last.getChanges().reversed())
                if (applyChange(change, true))
                    break
            actions.remove(last)
            redo.add(last)
            cached = null
            canvas.refreshSelection()
        }
    }

    @Suppress("LoopToCallChain")
    fun redo() {
        if (redo.isNotEmpty()) {
            ignore = true
            val last = redo.last()
            for (change in last.getChanges())
                if (applyChange(change, false))
                    break
            redo.remove(last)
            actions.add(last)
            cached = null
            canvas.refreshSelection()
        }
    }

    private fun applyChange(change: Change, undo: Boolean) : Boolean {
        when(change.type) {
            ChangeType.ADD, ChangeType.REMOVE, ChangeType.CHANGE -> {
                return if(undo)
                    applyChange(change, change.type == ChangeType.REMOVE, change.type == ChangeType.ADD)
                else
                    applyChange(change, change.type == ChangeType.ADD, change.type == ChangeType.REMOVE)
            }
            ChangeType.ORDER -> {
                if(change.value is List<*>) {
                    val list = change.value as List<Int>
                    if (undo)
                        widgets.getAll().move(widgets.getAll()[list[1]], list[0])
                    else
                        widgets.getAll().move(widgets.getAll()[list[0]], list[1])
                }
                return false
            }
        }
    }

    private fun applyChange(change: Change, add: Boolean, remove: Boolean): Boolean {
        val memento: Memento = change.value as Memento
        if (add) {
            val widget = WidgetMementoBuilderAdapter(memento).build(change.id)
            widgets.add(widget)
            widget.restore(memento)
        } else {
            if(cached == null || cached?.identifier != change.id) {
                for (node in widgets.getAll()) {
                    if (node is WidgetGroup) {
                        if (node.identifier == change.id) {
                            cached = node
                            break
                        }
                    }
                }
            }

            if(cached != null) {
                if (remove) {
                    cached!!.setSelected(false)
                    widgets.remove(cached!!)
                    return true
                } else
                    cached?.restore(memento)
            }
        }
        return false
    }

    fun addSingle(add: ChangeType, widget: WidgetGroup) {
        start(widget)
        record(add, widget)
        finish()
    }
}