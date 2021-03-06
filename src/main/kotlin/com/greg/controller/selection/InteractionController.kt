package com.greg.controller.selection

import com.greg.controller.widgets.WidgetsController
import com.greg.model.cache.archives.widget.WidgetData
import com.greg.model.cache.archives.widget.WidgetDataConverter
import com.greg.model.widgets.JsonSerializer
import com.greg.model.widgets.WidgetType
import com.greg.model.widgets.type.Widget
import com.greg.model.widgets.type.WidgetContainer
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent

class InteractionController(val widgets: WidgetsController) {

    fun paste() {
        val clipboard = Clipboard.getSystemClipboard()
        val string = clipboard.string

        //Check clipboard isn't empty
        if (string.isNullOrEmpty())
            return

        //Split clipboard into lines
        val lines = string.split("\n")

        //Clear the current selection
        widgets.clearSelection()

        val widgetList = arrayListOf<Widget>()

        //For each line
        for (line in lines) {

            val data = JsonSerializer.deserializer(line, WidgetData::class.java)

            if(data == null) {
                println("Error deserializing WidgetData $line")
                continue
            }

            val widget = WidgetDataConverter.create(data)

            //Apply selection
            if (!widget.isSelected())
                widget.setSelected(true, false)

            widgetList.add(widget)
        }

        //Display all widgets at once
        widgets.addAll(widgetList.toTypedArray())//TODO apply to parent if pasted on a container?

        //Select all widgets
        WidgetsController.selection.addAll(widgetList)
    }

    fun copy() {
        val clipboard = Clipboard.getSystemClipboard()

        //Deselect children to prevent duplicates
        widgets.deselectChildren()

        //Convert selected widget's into a string of attribute values
        val array = widgets.getSelection().map { it.toJson() }

        //Set the clipboard
        if (array.isNotEmpty()) {
            val content = ClipboardContent()
            content.putString(array.joinToString("\n"))
            clipboard.setContent(content)
        }
    }

    fun clone() {
        val list = widgets.getSelection().toList()

        val map = mutableMapOf<Widget, Widget?>()

        WidgetsController.selection.removeAll(list)

        list.forEach { widget ->
            val data = widget.toData()
            val clone = WidgetDataConverter.create(data, true)
            widget.setSelected(false, false)
            clone.setSelected(true, false)
            map[clone] = widget.getParent()
        }

        WidgetsController.selection.addAll(map.keys)
        widgets.addAll(map.filterValues { it == null }.keys.toTypedArray())
        map.filterValues { it != null }.forEach { widget, parent -> (parent as? WidgetContainer)?.getChildren()?.add(widget) }
    }

    private fun isWidget(name: String): Boolean {
        return WidgetType.forString(name) != null
    }
}