package com.greg.view

import com.greg.controller.widgets.WidgetsController
import com.greg.model.widgets.WidgetType
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.control.CheckBoxTreeItem
import javafx.scene.control.SelectionMode
import javafx.scene.control.TabPane
import javafx.scene.control.TreeItem
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.paint.Color
import javafx.scene.shape.StrokeType
import javafx.util.Callback
import org.controlsfx.control.CheckTreeView
import org.controlsfx.control.GridView
import org.controlsfx.control.cell.ColorGridCell
import tornadofx.*
import java.util.*



class LeftPane : View() {

    private val widgets: WidgetsController by inject()
    private var checkTreeView: CheckTreeView<String>? = null
    private val rootTreeItem = CheckBoxTreeItem("Root")

    init {
        widgets.getAll().addListener(ListChangeListener {
            it.next()
            //Get items changed
            val list = if (it.wasAdded()) it.addedSubList else it.removed

            list?.forEach { widget ->
                if(it.wasAdded()) {
                    val item = CustomTreeItem(widget.name, widget.identifier)
                    rootTreeItem.children.add(item)
                    item.selectedProperty().bindBidirectional(widget.selectedProperty())
                } else if(it.wasRemoved()) {
                    //TODO test
                    rootTreeItem.children.removeAll(
                            rootTreeItem.children
                            .filterIsInstance<CustomTreeItem>()
                            .filter { it.identifier == widget.identifier }
                    )
                }
            }
        })
    }

    override val root = splitpane(Orientation.VERTICAL) {
        minWidth = 290.0
        prefWidth = 290.0
        tabpane {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            tab("Components") {
                val types = WidgetType.values().toList()

                tilepane {
                    hgap = 10.0
                    vgap = 10.0

                    for(type in types) {
                        vbox {
                            padding = Insets(10.0, 0.0, 0.0, 0.0)
                            val image = resources.image("${type.name.toLowerCase()}.png")
                            stackpane {
                                rectangle {
                                    width = 46.0
                                    height = 46.0
                                    fill = Color.TRANSPARENT
                                    strokeWidth = 3.0
                                    stroke = Color.GRAY
                                    arcWidth = 10.0
                                    arcHeight = 10.0
                                    strokeType = StrokeType.INSIDE
                                }
                                imageview(image)
                            }

                            stackpane {
                                text(type.name.toLowerCase().capitalize())
                            }

                            setOnDragDetected { event ->
                                val db = startDragAndDrop(TransferMode.MOVE)
                                db.dragView = image
                                val cc = ClipboardContent()
                                cc.putString(type.name)
                                db.setContent(cc)

                                event.consume()
                            }
                        }
                    }
                }
            }
            tab("Sprites") {
                disableDelete()
                val list = FXCollections.observableArrayList<Color>()

                val colorGrid = GridView(list)

                colorGrid.cellFactory = Callback { ColorGridCell() }
                val r = Random(System.currentTimeMillis())
                for (i in 0..800) {
                    list.add(Color(r.nextDouble(), r.nextDouble(), r.nextDouble(), 1.0))
                }
                add(colorGrid)
            }
        }
        tabpane {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            tab("Hierarchy") {

                rootTreeItem.isExpanded = true

                val tree = CheckTreeView(rootTreeItem)
                tree.selectionModel.selectionMode = SelectionMode.MULTIPLE
                tree.selectionModel.selectedItems.addListener(ListChangeListener<TreeItem<String>> { c -> println(c.list) })

                checkTreeView = tree
                add(tree)
            }
        }
    }
}