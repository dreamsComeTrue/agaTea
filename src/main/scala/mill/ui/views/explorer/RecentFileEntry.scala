// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.views.explorer

import java.lang

import javafx.beans.binding.{Bindings, BooleanBinding}
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections._
import javafx.css.PseudoClass
import javafx.scene.control._
import javafx.scene.input._
import javafx.scene.layout.{AnchorPane, VBox}
import javafx.scene.text.Text
import mill.Resources
import mill.controller.AppController
import mill.model.ProjectsRepository
import mill.resources.Resource
import mill.ui.controls.TitledPaneContext
import org.apache.commons.io.FilenameUtils
import scalafx.collections.ObservableBuffer

class RecentFileEntry() {
  private var projectPane: TitledPaneContext = _
  private val projectScroll = new ScrollPane
  private val resourcesList = new ObservableBuffer[Resource]
  private val openedResourcesListView = new ListView[Resource](resourcesList)
  private val tmpText = new Text
  final private val dragSource = new SimpleObjectProperty[ListCell[Resource]]
  private var dragIndex = -1

  init()

  private def init(): Unit = {
    val label = new Label(Resources.RECENT_FILES)
    val closeNode = new Label("X")
    closeNode.setTooltip(new Tooltip(Resources.CLOSE_ALL_RESOURCES))

    val closeNodeStyleNormal = "-fx-text-fill: gray;"
    val closeNodeStyleHighlighted = "-fx-text-fill: white;"
    closeNode.setStyle(closeNodeStyleNormal)
    closeNode.setOnMouseEntered((_: MouseEvent) => closeNode.setStyle(closeNodeStyleHighlighted))
    closeNode.setOnMouseExited((_: MouseEvent) => closeNode.setStyle(closeNodeStyleNormal))

    val groupingBox = new AnchorPane(label, closeNode)
    AnchorPane.setRightAnchor(closeNode, -5.0)

    ProjectExplorer.instance().getGlobalScroll.widthProperty.addListener(new ChangeListener[Number] {
      override def changed(observableValue: ObservableValue[_ <: Number], t: Number, newValue: Number): Unit = {
        groupingBox.setPrefWidth(newValue.doubleValue - 35)
      }
    })

    projectScroll.setFitToWidth(true)
    projectScroll.setFitToHeight(true)
    projectScroll.setMinHeight(0)
    projectScroll.setMaxHeight(0)

    val projectContent = new VBox(openedResourcesListView)
    projectScroll.setContent(projectContent)

    projectPane = new TitledPaneContext("", projectScroll)
    projectPane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY)
    projectPane.setGraphic(groupingBox)

    closeNode.setOnMouseClicked((event: MouseEvent) => {
      val openedFiles = ProjectsRepository.instance().getOpenFiles

      openedFiles.keySet.forEach((file: String) => ProjectExplorer.instance().closeResourceInEditor(file))

      event.consume()

      ProjectsRepository.instance().clearAllOpenFiles()
      projectPane.setExpanded(true)
    })

    val openFiles: ObservableMap[String, Resource] = ProjectsRepository.instance().getOpenFiles
    openFiles.addListener(new MapChangeListener[String, Resource] {
      override def onChanged(change: MapChangeListener.Change[_ <: String, _ <: Resource]): Unit = {
        if (change.wasAdded) {
          resourcesList.add(change.getValueAdded)
        }
        else if (change.wasRemoved) {
          resourcesList.remove(change.getValueRemoved)
        }

        val paneSize = resourcesList.size * 20 + 4
        projectScroll.setMinHeight(paneSize)
        projectScroll.setMaxHeight(paneSize)

        var maxWidth = 0.0

        for (res <- resourcesList) {
          val name = res.getName
          tmpText.setText(name)

          val width = tmpText.getLayoutBounds.getWidth
          if (width > maxWidth) maxWidth = width
        }

        maxWidth = maxWidth + 320

        if (maxWidth < 100) maxWidth = 120
        // ProjectExplorer.get ().getContent ().setPrefWidth (maxWidth);
        //  ProjectExplorer.get ().getContent ().setMaxWidth (maxWidth);
      }
    })

    openedResourcesListView.setStyle("-fx-border-color: transparent;")
    openedResourcesListView.setCellFactory((_: ListView[Resource]) => new ProjectItemCell)

    openedResourcesListView.setOnMouseClicked((event: MouseEvent) => {
      if (event.getClickCount == 1) { //  Grab the selected item within package
        val res = openedResourcesListView.getSelectionModel.getSelectedItem

        if (res != null) {
          ProjectExplorer.instance().openResource(res)

          val projectEntry = ProjectExplorer.instance().getCurrentProjectEntry

          if (projectEntry != null) {
            val tree = projectEntry.getTree
            tree.getSelectionModel.clearSelection()

            ProjectExplorer.instance().getSelectedTreeItems.clear()
          }
        }
      }

      handleItemSelection()
    })

    openedResourcesListView.setOnKeyPressed((event: KeyEvent) => {
      if (event.getCode eq KeyCode.DOWN) {
        val selectedItem = openedResourcesListView.getSelectionModel.getSelectedItem

        if (openedResourcesListView.getItems.get(openedResourcesListView.getItems.size - 1) eq selectedItem) {
          val projectEntry = ProjectExplorer.instance().getCurrentProjectEntry

          if (projectEntry != null) {
            val tree = projectEntry.getTree
            tree.getSelectionModel.select(tree.getRoot.getChildren.get(0))
            tree.requestFocus()

            ProjectExplorer.instance().getSelectedItems.clear()
            openedResourcesListView.getSelectionModel.clearSelection()
            openedResourcesListView.getSelectionModel.select(-1)
            event.consume()
          }
        }
      }
      else if (event.getCode eq KeyCode.ENTER) {
        val res = openedResourcesListView.getSelectionModel.getSelectedItem
        ProjectExplorer.instance().openResource(res)
      }

      handleItemSelection()
    })

    openedResourcesListView.getSelectionModel.getSelectedItems.addListener(new ListChangeListener[Resource] {
      override def onChanged(change: ListChangeListener.Change[_ <: Resource]): Unit = {
        handleItemSelection()
      }
    })

    AppController.instance().addFXStageInitializer(projectPane)
  }

  private def handleItemSelection(): Unit = {
    val items = openedResourcesListView.getSelectionModel.getSelectedItems

    if (!items.isEmpty) {
      ProjectExplorer.instance().getSelectedTreeItems.clear()
      ProjectExplorer.instance().getSelectedItems.setAll(items)
    }
    else ProjectExplorer.instance().getSelectedItems.clear()
  }

  def getProjectPane: TitledPane = projectPane

  def getOpenedResourcesListView: ListView[Resource] = openedResourcesListView

  final private val highlighted = PseudoClass.getPseudoClass("highlighted")

  private[explorer] class ProjectItemCell() extends ListCell[Resource] {
    // indicates whether the cell should be highlighted:
    private val highlightCell: BooleanBinding = Bindings.createBooleanBinding(() => getItem != null && ProjectExplorer.instance().getSelectedItems.contains(getItem), itemProperty, ProjectExplorer.instance().getSelectedItems)
    // listener for the binding above
    // note this has to be scoped to persist alongside the cell, as the binding
    // will use weak listeners, and we need to avoid the listener getting gc'd:
    private val listener: ChangeListener[lang.Boolean] = (_: ObservableValue[_ <: lang.Boolean], _: lang.Boolean, isHighlighted: lang.Boolean) => pseudoClassStateChanged(highlighted, isHighlighted)

    highlightCell.addListener(listener)

    this.setOnDragDetected((event: MouseEvent) => {
      if (!isEmpty) {
        val db = startDragAndDrop(TransferMode.MOVE)
        val cc = new ClipboardContent
        cc.put(DataFormat.PLAIN_TEXT, getItem.getName)
        db.setContent(cc)
        dragSource.set(this)
        dragIndex = openedResourcesListView.getItems.indexOf(getItem)
      }
      event.consume()
    })

    this.setOnDragOver((event: DragEvent) => {
      val db = event.getDragboard
      if (db.hasString && dragSource.get != null) {
        if (!ProjectItemCell.this.getStyleClass.contains("drag-node-entered")) {
          if (ProjectItemCell.this.getStyleClass.contains("drag-node-exited")) ProjectItemCell.this.getStyleClass.remove("drag-node-exited")
          ProjectItemCell.this.getStyleClass.add("drag-node-entered")
        }

        val valueToMove = event.getDragboard.getString

        if (!(valueToMove == getItem.getName)) { // We accept the transfer!!!!!
          event.acceptTransferModes(TransferMode.MOVE)
        }
      }
      event.consume()
    })

    this.setOnDragDone((event: DragEvent) => {
      dragSource.set(null)
      event.consume()
    })

    this.setOnDragExited((_: DragEvent) => {
      if (!ProjectItemCell.this.getStyleClass.contains("drag-node-exited")) {
        if (ProjectItemCell.this.getStyleClass.contains("drag-node-entered")) ProjectItemCell.this.getStyleClass.remove("drag-node-entered")
        ProjectItemCell.this.getStyleClass.add("drag-node-exited")
      }
    })

    this.setOnDragDropped((event: DragEvent) => {
      val db = event.getDragboard
      if (db.hasString && dragSource.get != null && event.getGestureTarget.isInstanceOf[RecentFileEntry#ProjectItemCell]) {
        val targetItem = event.getGestureTarget.asInstanceOf[RecentFileEntry#ProjectItemCell]
        if (targetItem.getItem != null) {
          val items = openedResourcesListView.getItems
          val targetIndex = items.indexOf(targetItem.getItem)
          val sourceIndex = items.indexOf(dragSource.get.getItem)
          val temp = items.remove(sourceIndex)
          items.add(targetIndex, temp)
          openedResourcesListView.getSelectionModel.clearAndSelect(targetIndex)
          event.setDropCompleted(true)
          dragSource.set(null)
        }
      }
      else event.setDropCompleted(false)
    })

    override def updateItem(item: Resource, empty: Boolean): Unit = {
      super.updateItem(item, empty)
      if (!empty) {
        val text = new Label(item.getName)
        val closeNode = new Label("X")
        val closeNodeStyleNormal = "-fx-text-fill: darkgray;"
        val closeNodeStyleHighlighted = "-fx-text-fill: white;"
        closeNode.setStyle(closeNodeStyleNormal)
        closeNode.setOnMouseEntered((_: MouseEvent) => closeNode.setStyle(closeNodeStyleHighlighted))
        closeNode.setOnMouseExited((_: MouseEvent) => closeNode.setStyle(closeNodeStyleNormal))
        closeNode.setOnMouseClicked((event: MouseEvent) => {
          event.consume()
          closeItem(item)
        })

        val bar = new AnchorPane
        bar.getChildren.addAll(text, closeNode)

        AnchorPane.setRightAnchor(closeNode, -2.0)

        bar.setOnMouseClicked((event: MouseEvent) => {
          if (event.getButton eq MouseButton.MIDDLE) {
            event.consume()
            closeItem(item)
          }
        })

        setGraphic(bar)
      }
    }

    private def closeItem(resource: Resource): Unit = {
      val path = FilenameUtils.normalize(resource.getFullPath)

      ProjectExplorer.instance().closeResourceInEditor(path)
      ProjectsRepository.instance().closeOpenFile(path)
    }
  }

}