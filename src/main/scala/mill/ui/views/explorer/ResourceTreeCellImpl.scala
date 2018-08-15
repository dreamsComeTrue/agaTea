// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.views.explorer

import javafx.beans.binding.{Bindings, BooleanBinding}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.css.PseudoClass
import javafx.scene.control.{Label, TreeCell, TreeItem, TreeView}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input._
import javafx.scene.layout.AnchorPane
import mill.model.ProjectsRepository
import mill.resources.Resource
import mill.resources.files.{ClassFile, PackageFile}
import mill.{Resources, Utilities}
import org.apache.commons.io.FilenameUtils

import scala.collection.JavaConverters._

final class ResourceTreeCellImpl(val projectEntry: ProjectEntry) extends TreeCell[Resource] {
  final private val highlighted = PseudoClass.getPseudoClass("highlighted")
  private var parentTree: TreeView[Resource] = _
  private var content: AnchorPane = _
  private var icon: ImageView = _
  private val label = new Label

  // indicates whether the cell should be highlighted:
  private val highlightCell: BooleanBinding = Bindings.createBooleanBinding(() => getTreeItem != null && ProjectExplorer.instance().getSelectedTreeItems.contains(getTreeItem), treeItemProperty, ProjectExplorer.instance().getSelectedTreeItems)
  // listener for the binding above
  // note this has to be scoped to persist alongside the cell, as the binding
  // will use weak listeners, and we need to avoid the listener getting gc'd:
  private val listener: ChangeListener[java.lang.Boolean] = (_: ObservableValue[_ <: java.lang.Boolean], _: java.lang.Boolean, isHighlighted: java.lang.Boolean) => pseudoClassStateChanged(highlighted, isHighlighted)

  init()

  private def init() {
    highlightCell.addListener(listener)

    this.parentTree = projectEntry.getTree

    val closeNode = new Label("X")
    val closeNodeStyleNormal = "-fx-text-fill: darkgray;"
    val closeNodeStyleHighlighted = "-fx-text-fill: white;"
    closeNode.setStyle(closeNodeStyleNormal)
    closeNode.setOnMouseEntered((_: MouseEvent) => closeNode.setStyle(closeNodeStyleHighlighted))
    closeNode.setOnMouseExited((_: MouseEvent) => closeNode.setStyle(closeNodeStyleNormal))
    closeNode.setOnMouseClicked((event: MouseEvent) => {
      val resource = getItem

      resource match {
        case packageFileResource: PackageFile =>
          packageFileResource.getClasses.forEach(this.closeItem)
        case _ => closeItem(getItem)
      }

      event.consume()
    })

    icon = Utilities.createImageView(Resources.Images.IMAGE_COFFEE, 15)
    content = new AnchorPane(icon, label, closeNode)

    AnchorPane.setRightAnchor(closeNode, 2.0)
    AnchorPane.setLeftAnchor(label, 14.0)

    this.setOnDragDetected((event: MouseEvent) => {
      val dragBoard = startDragAndDrop(TransferMode.MOVE)
      val content1 = new ClipboardContent
      content1.put(DataFormat.PLAIN_TEXT, getItem.getName)
      dragBoard.setContent(content1)
      projectEntry.setDraggedResource(this.getItem)
      event.consume()
    })

    this.setOnDragDone((dragEvent: DragEvent) => {
      projectEntry.setDraggedResource(null)
      dragEvent.consume()
    })

    this.setOnDragExited((event: DragEvent) => {
      def foo(event: DragEvent) = if (!ResourceTreeCellImpl.this.getStyleClass.contains("drag-node-exited")) {
        if (ResourceTreeCellImpl.this.getStyleClass.contains("drag-node-entered")) ResourceTreeCellImpl.this.getStyleClass.remove("drag-node-entered")
        ResourceTreeCellImpl.this.getStyleClass.add("drag-node-exited")
      }

      foo(event)
    })
    this.setOnDragOver((dragEvent: DragEvent) => {
      if (projectEntry.getDraggedResource != null) if (!projectEntry.getDraggedResource.isInstanceOf[PackageFile] && !getItem.isInstanceOf[PackageFile]) {
        if (!ResourceTreeCellImpl.this.getStyleClass.contains("drag-node-entered")) {
          if (ResourceTreeCellImpl.this.getStyleClass.contains("drag-node-exited")) ResourceTreeCellImpl.this.getStyleClass.remove("drag-node-exited")
          ResourceTreeCellImpl.this.getStyleClass.add("drag-node-entered")
        }
        val valueToMove = dragEvent.getDragboard.getString
        if (!(valueToMove == getItem.getName)) { // We accept the transfer!!!!!
          dragEvent.acceptTransferModes(TransferMode.MOVE)
        }
      }
      dragEvent.consume()
    })

    this.setOnDragDropped((dragEvent: DragEvent) => {
      System.out.println("Drag dropped on " + getItem)

      if (projectEntry.getDraggedResource != null) {
        val itemToMove = ResourceTreeCellImpl.search(parentTree.getRoot, projectEntry.getDraggedResource)
        val newParent = ResourceTreeCellImpl.search(parentTree.getRoot, getItem)

        if ((itemToMove ne newParent) && !(itemToMove.getParent == newParent) && !ResourceTreeCellImpl.isInParentTree(newParent, itemToMove)) { // Remove from former parent.
          if (itemToMove.getParent != null) {
            val treeParent = itemToMove.getParent
            val parent = treeParent.getValue
            val resourceToRemove = itemToMove.getValue

            parent match {
              case file: PackageFile => file.removeResource(resourceToRemove)
              case _ => if (parent == null) { // We are at the root
                projectEntry.getProject.getPackageFiles.remove(resourceToRemove)
              }
            }
          }

          // Add to new parent.
          val resourceToAdd = itemToMove.getValue
          val packageFile = newParent.getValue.asInstanceOf[PackageFile]

          if (!resourceToAdd.isInstanceOf[PackageFile]) packageFile.getClasses.add(resourceToAdd.asInstanceOf[ClassFile])
          else packageFile.getPackages.add(resourceToAdd.asInstanceOf[PackageFile])

          newParent.setExpanded(true)
        }

        projectEntry.setDraggedResource(null)
      }

      dragEvent.consume()
    })
  }

  private def closeItem(resource: Resource): Unit = {
    val path = FilenameUtils.normalize(resource.getFullPath)

    ProjectExplorer.instance().closeResourceInEditor(path)
    ProjectsRepository.instance().closeOpenFile(path)
  }

  override protected def updateItem(item: Resource, empty: Boolean): Unit = {
    super.updateItem(item, empty)
    if (getItem != null) { //if (content == null)
      item match {
        case file: PackageFile => if (file.isRawDirectory) icon.setImage(new Image(Utilities.getResource(Resources.Images.IMAGE_FOLDER)))
        else icon.setImage(new Image(Utilities.getResource(Resources.Images.IMAGE_PACKAGE)))
        case _ => icon.setImage(new Image(Utilities.getResource(Resources.Images.IMAGE_COFFEE)))
      }

      label.setText(getItem.getName)
      setGraphic(content)
    }
    else setGraphic(null)
  }
}

object ResourceTreeCellImpl {
  def isInParentTree(currentNode: TreeItem[Resource], possibleParent: TreeItem[Resource]): Boolean = {
    val currentParent = currentNode.getParent
    if (currentParent != null && possibleParent != null) if (possibleParent == currentParent) return true
    else if (isInParentTree(currentParent, possibleParent)) return true
    false
  }

  def search(currentNode: TreeItem[Resource], valueToSearch: Resource): TreeItem[Resource] = {
    var result: TreeItem[Resource] = null

    if (currentNode.getValue != null && currentNode.getValue == valueToSearch) result = currentNode
    else if (!currentNode.isLeaf) {
      for (child <- currentNode.getChildren.asScala) {
        result = search(child, valueToSearch)

        if (result != null) {
          return result
        }
      }
    }

    result
  }
}