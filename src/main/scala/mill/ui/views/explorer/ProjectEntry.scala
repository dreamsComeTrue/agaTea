// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.views.explorer

import java.lang

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.{ListChangeListener, ObservableList}
import javafx.event.ActionEvent
import javafx.scene.control._
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input._
import javafx.scene.layout.{AnchorPane, HBox}
import mill.controller.{AppController, FXStageInitializer, GlobalState}
import mill.model.ProjectsRepository
import mill.resources.files.{ClassFile, PackageFile}
import mill.resources.{Project, Resource}
import mill.ui.controls.{ConfirmContentBar, EnterFieldContentBar, LabelSeparatorMenuItem, TitledPaneContext}
import mill.{Resources, Utilities}
import org.apache.commons.io.FilenameUtils

import scala.collection.JavaConverters._

class ProjectEntry(val imagePath: String, val title: String, var project: Project) extends FXStageInitializer {
  private var projectPane: TitledPaneContext = _
  private var projectPaneContextMenuClicked = false
  private var projectScroll = new ScrollPane
  private var tree: TreeView[Resource] = _
  private var rootItem = new TreeItem[Resource](null)
  private var draggedResource: Resource = _
  private val contextMenu = new ContextMenu

  init()

  private def init() {
    val image = new Image(Utilities.getResource(imagePath))
    val imageView1 = new ImageView(image)
    val iconSize = 17.0
    imageView1.setFitWidth(iconSize)
    imageView1.setFitHeight(iconSize)

    val imageView: ImageView = Utilities.createImageView(Resources.Images.IMAGE_COFFEE, 15)
    val label = new Label(title)
    val groupingBox = new HBox(imageView, label)
    val closeNode = new Label("X")
    closeNode.setTooltip(new Tooltip(Resources.CLOSE_PROJECT))

    val closeNodeStyleNormal = "-fx-text-fill: gray;"
    val closeNodeStyleHighlighted = "-fx-text-fill: white;"
    closeNode.setStyle(closeNodeStyleNormal)
    closeNode.setOnMouseEntered((_: MouseEvent) => closeNode.setStyle(closeNodeStyleHighlighted))
    closeNode.setOnMouseExited((_: MouseEvent) => closeNode.setStyle(closeNodeStyleNormal))

    closeNode.setOnMouseClicked((event: MouseEvent) => {
      event.consume()
      ProjectExplorer.instance().closeAllResourcesInEditor(project)
      ProjectsRepository.instance().removeProject(project.getFullPath)
    })

    val anchor = new AnchorPane(groupingBox, closeNode)
    AnchorPane.setRightAnchor(closeNode, -5.0)

    ProjectExplorer.instance().getGlobalScroll.widthProperty.addListener(new ChangeListener[Number] {
      override def changed(observableValue: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
        anchor.setPrefWidth(newValue.doubleValue - 35)
      }
    })

    projectScroll.setFitToWidth(true)
    projectScroll.setFitToHeight(true)
    projectScroll.setMinHeight(0)
    projectScroll.setMaxHeight(0)

    projectPane = new TitledPaneContext("", projectScroll)
    projectPane.setFocusTraversable(false)
    projectPane.setContentDisplay(ContentDisplay.GRAPHIC_ONLY)
    projectPane.setOnMouseClicked((event: MouseEvent) => {
      if (event.getButton == MouseButton.SECONDARY) {
        projectPaneContextMenuClicked = true
        ProjectExplorer.instance().getSelectedTreeItems.clear()
      }
    })

    AppController.instance().addFXStageInitializer(projectPane)

    rootItem = new TreeItem[Resource](null)
    rootItem.setExpanded(true)

    tree = new TreeView[Resource](rootItem)
    tree.setShowRoot(false)
    tree.getStyleClass.add("tree-view-no-border")
    tree.setCellFactory((p: TreeView[Resource]) => new ResourceTreeCellImpl(this))
    tree.setContextMenu(contextMenu)

    projectScroll.setContent(tree)

    tree.setOnKeyPressed((event: KeyEvent) => {
      val keyCode = event.getCode

      if (keyCode eq KeyCode.UP) {
        val selectedItem = tree.getSelectionModel.getSelectedItem

        if (tree.getRoot.getChildren.get(0) eq selectedItem) {
          val listView = ProjectExplorer.instance().getRecentFileEntry.getOpenedResourcesListView

          if (listView.getItems.size > 0) {
            tree.getSelectionModel.clearSelection()

            listView.requestFocus()
            listView.getSelectionModel.select(listView.getItems.size - 1)
          }
        }
      }
      else if (keyCode eq KeyCode.ENTER) { //  Grab the selected item within package
        val res = tree.getSelectionModel.getSelectedItem.getValue
        if (!res.isInstanceOf[PackageFile]) ProjectExplorer.instance().openResource(res)
      }

      handleItemSelection()
    })

    tree.setOnMouseClicked((_: MouseEvent) => {
      handleItemSelection()
      openSelectedItem()
      ProjectExplorer.instance().getSelectedItems.clear()
    })

    tree.getSelectionModel.getSelectedItems.addListener(new ListChangeListener[TreeItem[Resource]] {
      override def onChanged(change: ListChangeListener.Change[_ <: TreeItem[Resource]]): Unit = {
        handleItemSelection()
      }
    })

    project.getPackageFiles.addListener(new ListChangeListener[PackageFile] {
      override def onChanged(change: ListChangeListener.Change[_ <: PackageFile]): Unit = {
        change.next

        if (change.wasRemoved) {
          val removedPackages = change.getRemoved

          for (packageFile <- removedPackages.asScala) {
            for (child <- rootItem.getChildren.asScala) {
              if (child == packageFile) {
                rootItem.getChildren.remove(child)
              }
            }
          }
        }
        else if (change.wasAdded) {
          val addedPackages = change.getAddedSubList

          for (packageFile <- addedPackages.asScala) {
            refreshPackage(rootItem, packageFile)
          }
        }
        alignProjectScrollSize()
      }
    })

    projectPane.setGraphic(anchor)
    projectPane.getGraphic.setOnContextMenuRequested((event: ContextMenuEvent) => contextMenu.show(projectPane.getGraphic, event.getScreenX, event.getScreenY))

    anchor.setPrefWidth(ProjectExplorer.instance().getGlobalScroll.getWidth - 35)

    AppController.instance().addFXStageInitializer(this)

    val contentBar = new EnterFieldContentBar
    val contentBarOKButton: Button = contentBar.getOkButton
    val contentBarLabel: Label = contentBar.getLabel
    val contentBarTextField: TextField = contentBar.getTextField
    val confirmBar = new ConfirmContentBar
    val confirmBarOKButton: Button = confirmBar.getOkButton
    val confirmBarCaptionLabel: Label = confirmBar.getCaptionLabel
    val confirmBarMessageLabel: Label = confirmBar.getMessageLabel
    val m1 = new MenuItem(Resources.ADD_CLASS, Utilities.createImageView(Resources.Images.IMAGE_COFFEE, 15))
    val m2 = new MenuItem(Resources.ADD_INTERFACE, Utilities.createImageView(Resources.Images.IMAGE_COFFEE, 15))
    val addPackageMenuItem = new MenuItem(Resources.ADD_PACKAGE, Utilities.createImageView(Resources.Images.IMAGE_PACKAGE, 15))
    addPackageMenuItem.setOnAction((_: ActionEvent) => {
      contentBarOKButton.setOnAction((_: ActionEvent) => {
        if (ProjectExplorer.instance().addPackageToProject(add = true, contentBarTextField.getText)) AppController.instance().hideContentBar()
      })

      contentBarTextField.setOnKeyPressed((event1: KeyEvent) => {
        if (event1.getCode eq KeyCode.ENTER) contentBarOKButton.fire()
      })

      contentBarTextField.textProperty.addListener(new ChangeListener[String] {
        override def changed(observableValue: ObservableValue[_ <: String], t: String, newValue: String): Unit = {
          if (!Utilities.isValidIdentifier(newValue)) contentBar.showValidationText(Resources.ENTERED_INVALID_PACKAGE_NAME)
          else if (!ProjectExplorer.instance().addPackageToProject(add = false, contentBarTextField.getText)) contentBar.showValidationText(Resources.ENTERED_DUPLICATED_PACKAGE_NAME)
          else contentBar.hideValidationText()
        }
      })

      contentBarTextField.setText("")
      contentBarLabel.setText(Resources.ENTER_PACKAGE_NAME)
      contentBar.show(Resources.Images.IMAGE_PACKAGE)
    })

    val renameMenuItem = new MenuItem(Resources.RENAME, Utilities.createImageView(Resources.Images.IMAGE_PEN, 13))
    renameMenuItem.setOnAction((_: ActionEvent) => {
      contentBarOKButton.setOnAction((_: ActionEvent) => AppController.instance().hideContentBar())

      contentBarTextField.setOnKeyPressed((event1: KeyEvent) => {
        if (event1.getCode eq KeyCode.ENTER) contentBarOKButton.fire()
      })

      contentBarTextField.textProperty.addListener(new ChangeListener[String] {
        override def changed(observableValue: ObservableValue[_ <: String], t: String, newValue: String): Unit = {
          if (!Utilities.isValidIdentifier(newValue)) contentBar.showValidationText(Resources.ENTERED_INVALID_PACKAGE_NAME)
          else if (!ProjectExplorer.instance().addPackageToProject(add = false, contentBarTextField.getText)) contentBar.showValidationText(Resources.ENTERED_DUPLICATED_PACKAGE_NAME)
          else contentBar.hideValidationText()
        }
      })

      if (projectPaneContextMenuClicked) {
        contentBarTextField.setText(title)
        projectPaneContextMenuClicked = false
      }
      else contentBarTextField.setText(ProjectExplorer.instance().getSelectedTreeItems.get(0).getValue.getName)
      contentBarLabel.setText(Resources.ENTER_NEW_NAME)
      contentBar.show(Resources.Images.IMAGE_PEN)
    })

    val ms = new LabelSeparatorMenuItem("Various", false, false)
    val deleteMenuItem = new MenuItem(Resources.DELETE, Utilities.createImageView(Resources.Images.IMAGE_TRASH, 13))

    deleteMenuItem.setOnAction((_: ActionEvent) => {
      confirmBarOKButton.setOnAction((_: ActionEvent) => {
        if (ProjectExplorer.instance().addPackageToProject(add = true, contentBarTextField.getText)) AppController.instance().hideContentBar()
      })

      if (projectPaneContextMenuClicked) {
        projectPaneContextMenuClicked = false

        confirmBarMessageLabel.setText(title)
      }
      else confirmBarMessageLabel.setText(ProjectExplorer.instance().getSelectedTreeItems.get(0).getValue.getName)

      confirmBarCaptionLabel.setText(Resources.CONFIRM_DELETE)
      confirmBar.show(Resources.Images.IMAGE_TRASH)
    })

    contextMenu.addEventFilter(MouseEvent.MOUSE_EXITED, (event: MouseEvent) => {
      contextMenu.hide()
      event.consume()
    })

    contextMenu.getItems.addAll(m1, m2, addPackageMenuItem, renameMenuItem, ms, deleteMenuItem)
    //	Refresh all packages and resources at start
    rootItem.getChildren.clear()

    for (childPackage <- project.getPackageFiles.asScala) {
      refreshPackage(rootItem, childPackage)
    }

    alignProjectScrollSize()
  }

  private def handleItemSelection(): Unit = {
    val items = tree.getSelectionModel.getSelectedItems

    if (items != null && !items.isEmpty) {
      ProjectExplorer.instance().getSelectedItems.clear()
      ProjectExplorer.instance().getSelectedTreeItems.setAll(items)
    }
    else ProjectExplorer.instance().getSelectedTreeItems.clear()
  }

  def openSelectedItem(): Unit = {
    val item = tree.getSelectionModel.getSelectedItem

    if (item != null) {
      val res = item.getValue

      if (res != null && res.isInstanceOf[ClassFile]) {
        val path = FilenameUtils.normalize(res.getFullPath)
        //  Try to open resource file in new editor
        if (!GlobalState.instance().isFileOpened(path)) AppController.instance().openResourceInEditor(res.getName, path, res)
        else AppController.instance().focusEditor(path)
      }
    }

    tree.requestFocus()
  }

  override def fxInitialize: Boolean = {
    val titleRegion = projectPane.getTitleRegion
    titleRegion.setOnDragOver((event: DragEvent) => {
      if (draggedResource != null && draggedResource.isInstanceOf[PackageFile]) {
        if (!projectPane.getStyleClass.contains("titled-node-entered")) {
          if (projectPane.getStyleClass.contains("titled-node-exited")) projectPane.getStyleClass.remove("titled-node-exited")
          projectPane.getStyleClass.add("titled-node-entered")
        }
        if (event.getDragboard.hasString) { // We accept the transfer!!!!!
          event.acceptTransferModes(TransferMode.MOVE)
        }
      }
    })

    titleRegion.setOnDragExited((event: DragEvent) => {
      if (!projectPane.getStyleClass.contains("titled-node-exited")) {
        if (projectPane.getStyleClass.contains("titled-node-entered")) projectPane.getStyleClass.remove("titled-node-entered")
        projectPane.getStyleClass.add("titled-node-exited")
      }
    })

    titleRegion.setOnDragDropped((dragEvent: DragEvent) => {
      val itemToMove = ResourceTreeCellImpl.search(rootItem, draggedResource)

      if (!rootItem.getChildren.contains(itemToMove)) { // Remove from former parent.
        if (itemToMove.getParent != null) {
          val treeParent = itemToMove.getParent
          val parent = treeParent.getValue

          parent match {
            case file: PackageFile => file.removeResource(itemToMove.getValue)
            case _ =>
          }
        }
        // Add to root
        // Add to new parent.
        val value = itemToMove.getValue

        value match {
          case file: PackageFile => project.getPackageFiles.add(file)
          case _ =>
        }
        alignProjectScrollSize()
      }

      draggedResource = null
      dragEvent.consume()
    })

    true
  }

  private def addInOrder(nodeToAttachTo: TreeItem[Resource], resource: TreeItem[Resource]): Unit = {
    val children = nodeToAttachTo.getChildren

    //	If we adding packages - go from the beginning of the node
    if (resource.getValue.isInstanceOf[PackageFile]) {
      var i = 0

      for (child <- children.asScala) {
        val res = child.getValue

        if (res.isInstanceOf[PackageFile]) if (res.getName.compareTo(resource.getValue.getName) > 0) {
          nodeToAttachTo.getChildren.add(i, resource)
          return
        }

        i += 1
      }

      nodeToAttachTo.getChildren.add(resource)
    }
    else {
      for (i <- children.size - 1 to 0 by -1) {
        val child = children.get(i)
        val res = child.getValue

        if (!res.isInstanceOf[PackageFile]) if (res.getName.compareTo(resource.getValue.getName) > 0) {
          nodeToAttachTo.getChildren.add(i, resource)
          return
        }
      }

      nodeToAttachTo.getChildren.add(resource)
    }
  }

  private def refreshPackage(nodeToAttachTo: TreeItem[Resource], packageFile: PackageFile): Unit = {
    val childClasses = packageFile.getClasses
    val childPackages = packageFile.getPackages
    val packageItem = new TreeItem[Resource](packageFile)

    addInOrder(nodeToAttachTo, packageItem)

    //	Add all sub-packages
    for (childPackage <- childPackages.asScala) {
      val childPackageItem = new TreeItem[Resource](childPackage)

      addInOrder(packageItem, childPackageItem)

      for (innerPackage <- childPackage.getPackages.asScala) {
        refreshPackage(childPackageItem, innerPackage)
      }
    }

    //	Add all belonging classes
    for (classFile <- childClasses.asScala) {
      val classItem = new TreeItem[Resource](classFile)

      addInOrder(packageItem, classItem)
    }

    packageItem.expandedProperty.addListener(new ChangeListener[lang.Boolean] {
      override def changed(observableValue: ObservableValue[_ <: lang.Boolean], t: lang.Boolean, t1: lang.Boolean): Unit = alignProjectScrollSize()
    })

    class PackagesListener extends ListChangeListener[PackageFile] {
      override def onChanged(c: ListChangeListener.Change[_ <: PackageFile]): Unit = {
        c.next

        if (c.wasRemoved) {
          val removedPackages = c.getRemoved

          for (removedPackage: PackageFile <- removedPackages.asScala) {
            val children: ObservableList[TreeItem[Resource]] = packageItem.getChildren

            for (resourceIt <- children.asScala) {
              val res = resourceIt.getValue

              if (res.isInstanceOf[PackageFile]) {
                if (res == removedPackage) {
                  packageItem.getChildren.remove(res)
                }
              }
            }
          }
        }
        else if (c.wasAdded) {
          val addedPackages = c.getAddedSubList

          for (addedPackage <- addedPackages.asScala) {
            refreshPackage(packageItem, addedPackage)
          }
        }

        alignProjectScrollSize()
      }
    }

    val packagesListener = new PackagesListener

    childPackages.removeListener(packagesListener)
    childPackages.addListener(packagesListener)

    class ClassFileListener extends ListChangeListener[ClassFile] {
      override def onChanged(c: ListChangeListener.Change[_ <: ClassFile]): Unit = {
        c.next
        if (c.wasRemoved) {
          val removedClasses = c.getRemoved.asScala

          for (removedClass <- removedClasses) {
            val children: ObservableList[TreeItem[Resource]] = packageItem.getChildren

            for (resourceIt <- children.asScala) {
              val res = resourceIt.getValue

              if (res.isInstanceOf[ClassFile]) {
                if (res == removedClass) {
                  packageItem.getChildren.remove(res)
                }
              }
            }
          }
        }
        else if (c.wasAdded) {
          val addedClasses = c.getAddedSubList.asScala

          for (addedClass <- addedClasses) {
            val classItem = new TreeItem[Resource](addedClass)
            addInOrder(packageItem, classItem)
          }
        }

        alignProjectScrollSize()
      }
    }

    val classFileListener = new ClassFileListener

    childClasses.removeListener(classFileListener)
    childClasses.addListener(classFileListener)
  }

  private def alignProjectScrollSize(): Unit = {
    val allItemsCount = countAllExpandedTreeItemChildren(rootItem)
    val paneSize = allItemsCount * 21 + 4

    projectScroll.setMinHeight(paneSize)
    projectScroll.setMaxHeight(paneSize)
    projectScroll.setMinHeight(paneSize)
    projectScroll.setMaxHeight(paneSize)
  }

  private def countAllExpandedTreeItemChildren(treeItem: TreeItem[Resource]): Int = {
    var count = 0

    if (treeItem != null) {
      val children = treeItem.getChildren.asScala

      if (children != null && treeItem.isExpanded) {
        count += children.size

        for (child <- children) {
          count += countAllExpandedTreeItemChildren(child)
        }
      }
    }
    count
  }

  def getProject: Project = project

  def getProjectPane: TitledPane = projectPane

  def getTree: TreeView[Resource] = tree

  def setDraggedResource(draggedResource: Resource): Unit = {
    this.draggedResource = draggedResource
  }

  def getDraggedResource: Resource = draggedResource
}
