// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.views

import java.io.{File, IOException}
import java.nio.file.attribute.{BasicFileAttributes, FileTime}
import java.nio.file.{FileSystems, Files, Path, Paths}
import java.text.NumberFormat
import java.{lang, util}

import javafx.beans.NamedArg
import javafx.beans.property.{ReadOnlyObjectWrapper, SimpleStringProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.{ActionEvent, Event, EventHandler, EventType}
import javafx.geometry.{Insets, Pos}
import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.image.ImageView
import javafx.scene.input.{KeyCode, KeyEvent, MouseButton, MouseEvent}
import javafx.scene.layout.{AnchorPane, HBox, Priority, VBox}
import mill.controller.AppController
import mill.ui.controls.PercentageTreeTableView
import mill.ui.views.FileSelectView.FileSelectViewMode.FileSelectViewMode
import mill.{Resources, Utilities}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class FileSelectView() extends AnchorPane {
  private var fileSelectedEvent: EventHandler[FileSelectView.FileSelectEvent] = _
  private var fileCanceledEvent: EventHandler[FileSelectView.FileSelectEvent] = _
  private var treeTableView: PercentageTreeTableView[FileTreeItemEntry] = _
  private var fileField: TextField = _
  private var pathField: TextField = _
  private val iconSize: Double = 17.0
  private val selectionString: SimpleStringProperty = new SimpleStringProperty("")
  private val selectedItems: util.List[TreeItem[FileTreeItemEntry]] = new util.ArrayList[TreeItem[FileTreeItemEntry]]
  private var selectedItemIndex: Int = 0

  init()

  private def init(): Unit = {
    val rootNode: TreeItem[FileTreeItemEntry] = prepareRootFileEntry
    val nameColumn: PercentageTreeTableView.PercentageTableColumn[FileTreeItemEntry, String] = prepareNameColumn
    val sizeColumn: PercentageTreeTableView.PercentageTableColumn[FileTreeItemEntry, FileTreeItemEntry] = prepareSizeColumn
    val createdColumn: PercentageTreeTableView.PercentageTableColumn[FileTreeItemEntry, FileTime] = prepareCreatedColumn

    treeTableView = new PercentageTreeTableView[FileTreeItemEntry]
    treeTableView.getTreeTableView.setPlaceholder(new Label(Resources.SELECT_DIRECTORY))
    treeTableView.getTreeTableView.setShowRoot(false)
    treeTableView.setPrefHeight(Integer.MAX_VALUE - 1)
    treeTableView.getTreeTableView.setRoot(rootNode)
    treeTableView.getTreeTableView.getColumns.addAll(nameColumn, sizeColumn, createdColumn)

    val headerBox: HBox = prepareHeaderPane
    val fieldsPane: SplitPane = prepareFieldsPane
    val boxPane: VBox = new VBox(headerBox, treeTableView, fieldsPane)

    boxPane.getStyleClass.add("project-explorer-background")

    VBox.setMargin(headerBox, new Insets(0, 0, 2, 0))
    VBox.setMargin(treeTableView, new Insets(0, 0, 4, 0))

    AnchorPane.setTopAnchor(boxPane, 2.0)
    AnchorPane.setLeftAnchor(boxPane, 2.0)
    AnchorPane.setRightAnchor(boxPane, 2.0)
    AnchorPane.setBottomAnchor(boxPane, 0.0)

    this.getChildren.addAll(boxPane)

    attachTreeSelectionToPathField()
    attachTreeMouseClicked()
    attachTreeKeyReleased()
    attachTreeEventFilter()
    attachTreeKeyPressed()
  }

  private def attachTreeKeyPressed(): Unit = {
    treeTableView.setOnKeyPressed((event: KeyEvent) => {
      val alreadySelected: Boolean = false
      event.consume()

      if (event.getCode == KeyCode.BACK_SPACE) if (selectionString.get.length > 0) {
        selectedItems.clear()
        selectedItemIndex = -1
        selectionString.set(selectionString.get.substring(0, selectionString.get.length - 1))
      }
      else if (!(event.getCode == KeyCode.ENTER)) {
        selectedItems.clear()
        selectedItemIndex = -1
        selectionString.set(selectionString.get + event.getText)
      }

      selectionString.addListener(new ChangeListener[String] {
        override def changed(observableValue: ObservableValue[_ <: String], t: String, newValue: String): Unit = {
          if (newValue.length > 0) {
            if (!selectedItems.isEmpty && !alreadySelected) selectNextItem()
            AppController.instance().setFooterMessageText(Resources.SEARCH + ": " + newValue)
          }
          else {
            selectedItemIndex = -1
            AppController.instance().setFooterMessageText("")
          }
        }
      })
    })
  }

  private def attachTreeEventFilter(): Unit = {
    treeTableView.addEventFilter(KeyEvent.KEY_PRESSED, (event: KeyEvent) => {
      if (event.getCode == KeyCode.UP) if (!selectedItems.isEmpty) event.consume()
      else if (event.getCode == KeyCode.DOWN) if (!selectedItems.isEmpty) event.consume()
    })
  }

  private def attachTreeKeyReleased(): Unit = {
    treeTableView.setOnKeyReleased((event: KeyEvent) => {
      val tree: TreeTableView[FileTreeItemEntry] = treeTableView.getTreeTableView
      val selectedNode: TreeItem[FileTreeItemEntry] = tree.getSelectionModel.getSelectedItem
      if (event.getCode == KeyCode.SPACE || event.getCode == KeyCode.ENTER) { //  If we are on 'dir' node - expand or collapse them, otherwise - handle file load
        if (selectedNode != null) if (!selectedNode.isLeaf) {
          selectedNode.setExpanded(!selectedNode.isExpanded)
          selectedItems.clear()
          selectedItemIndex = -1
          selectionString.set("")
        }
        else handleFileOpen()
      }
      else if (event.getCode == KeyCode.ESCAPE) {
        selectedItems.clear()
        selectedItemIndex = -1
        selectionString.set("")
      }
      else if (event.getCode == KeyCode.UP && !selectedItems.isEmpty) selectPreviousItem()
      else if (event.getCode == KeyCode.DOWN && !selectedItems.isEmpty) selectNextItem()
    })
  }

  private def attachTreeMouseClicked(): Unit = { //  Double clicked mouse selection
    treeTableView.getTreeTableView.setOnMouseClicked((event: MouseEvent) => {
      if (event.getButton == MouseButton.PRIMARY && event.getClickCount == 2) handleFileOpen()
    })
  }

  private def attachTreeSelectionToPathField(): Unit = {
    treeTableView.getTreeTableView.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[TreeItem[FileTreeItemEntry]] {
      override def changed(observableValue: ObservableValue[_ <: TreeItem[FileTreeItemEntry]], t: TreeItem[FileTreeItemEntry], newValue: TreeItem[FileTreeItemEntry]): Unit = {
        if (newValue != null && newValue.getValue != null) {
          val f: File = newValue.getValue.getFile
          if (f != null) if (f.isDirectory) pathField.setText(f.getAbsolutePath)
          else {
            pathField.setText(f.getParent)
            fileField.setText(f.getName)
          }
        }
      }
    })
  }

  private def prepareHeaderPane: HBox = {
    val favoritesDirs: ObservableList[MenuItem] = FXCollections.observableArrayList[MenuItem]
    val addFavorite: SplitMenuButton = new SplitMenuButton

    addFavorite.setGraphic(Utilities.createImageView(Resources.Images.IMAGE_ADD, 10))
    addFavorite.setTooltip(new Tooltip(Resources.ADD_FAVORITE_DIRECTORY))

    val removeFavorite: SplitMenuButton = new SplitMenuButton
    removeFavorite.setGraphic(Utilities.createImageView(Resources.Images.IMAGE_REMOVE, 10))
    removeFavorite.setTooltip(new Tooltip(Resources.REMOVE_FAVORITE_DIRECTORY))

    val collapseAllButton: Button = Utilities.createButton(Resources.Images.IMAGE_COLLAPSE, 20, Utilities.DEFAULT_IMAGE_PADDING)
    collapseAllButton.setTooltip(new Tooltip(Resources.COLLAPSE_ALL))

    addFavorite.setOnAction((_: ActionEvent) => {
      val path: String = pathField.getText

      if (!path.isEmpty) {
        var found: Boolean = false

        for (items <- favoritesDirs.asScala) {
          if (items.getText == path) {
            found = true
          }
        }

        if (!found) {
          val it: MenuItem = new MenuItem(path)
          favoritesDirs.add(it)
          addFavorite.getItems.addAll(it)
        }
      }
    })

    removeFavorite.setOnAction((_: ActionEvent) => {
      //  if (favoriteListView.getSelectionModel ().getSelectedItem () != null)

      //      favoritesDirsNames.remove (favoriteListView.getSelectionModel ().getSelectedItem ());
      ///      favoriteListView.getSelectionModel ().select (null);
    })

    new HBox(addFavorite, removeFavorite, collapseAllButton)
  }

  private def prepareFieldsPane: SplitPane = {
    val pathLabel: Label = new Label(Resources.PATH)
    pathField = new TextField

    val fileLabel: Label = new Label(Resources.FILE)
    fileField = new TextField("")

    val okButton: Button = Utilities.createButton(Resources.Images.IMAGE_ACCEPT, 20.0, Utilities.DEFAULT_IMAGE_PADDING)
    okButton.setOnAction((_: ActionEvent) => handleFileOpen())
    okButton.setTooltip(new Tooltip(Resources.ACCEPT))

    val cancelButton: Button = Utilities.createButton(Resources.Images.IMAGE_REJECT, 20.0, Utilities.DEFAULT_IMAGE_PADDING)
    cancelButton.setTooltip(new Tooltip(Resources.REJECT))
    cancelButton.setOnAction((_: ActionEvent) => {
      if (fileCanceledEvent != null) {
        val fse: FileSelectView.FileSelectEvent = new FileSelectView.FileSelectEvent(null, null)
        fileCanceledEvent.handle(fse)
      }
    })

    val box1: HBox = new HBox(pathLabel, pathField)
    box1.setAlignment(Pos.CENTER)

    val box2: HBox = new HBox(fileLabel, fileField, okButton, cancelButton)
    box2.setAlignment(Pos.CENTER)

    val fieldsPane: SplitPane = new SplitPane
    fieldsPane.setDividerPositions(0.65)
    fieldsPane.getItems.addAll(box1, box2)

    pathLabel.setMinWidth(30)

    HBox.setHgrow(pathField, Priority.ALWAYS)
    HBox.setMargin(pathField, new Insets(0, 5, 0, 0))
    HBox.setMargin(fileLabel, new Insets(0, 0, 0, 5))

    fileLabel.setMinWidth(25)

    HBox.setHgrow(fileField, Priority.ALWAYS)
    HBox.setHgrow(okButton, Priority.ALWAYS)
    HBox.setHgrow(cancelButton, Priority.ALWAYS)

    fieldsPane
  }

  private def prepareCreatedColumn: PercentageTreeTableView.PercentageTableColumn[FileTreeItemEntry, FileTime] = {
    val createdColumn: PercentageTreeTableView.PercentageTableColumn[FileTreeItemEntry, FileTime] = new PercentageTreeTableView.PercentageTableColumn[FileTreeItemEntry, FileTime](Resources.CREATED_ON)

    createdColumn.setCellValueFactory((p: TreeTableColumn.CellDataFeatures[FileTreeItemEntry, FileTime]) => {
      def foo(p: TreeTableColumn.CellDataFeatures[FileTreeItemEntry, FileTime]): ReadOnlyObjectWrapper[FileTime] = {
        if ((p.getValue.getValue != null) && (p.getValue.getValue.getFile != null)) {
          try {
            val path: Path = Paths.get(p.getValue.getValue.getFile.getAbsolutePath)
            val attributes: BasicFileAttributes = Files.readAttributes(path, classOf[BasicFileAttributes])
            return new ReadOnlyObjectWrapper[FileTime](attributes.creationTime)
          } catch {
            case _: IOException =>

            //  e.printStackTrace ();
          }
          null
        }
        else null
      }

      foo(p)
    })

    createdColumn.setCellFactory((_: TreeTableColumn[FileTreeItemEntry, FileTime]) => new TreeTableCell[FileTreeItemEntry, FileTime]() {
      override protected def updateItem(item: FileTime, empty: Boolean): Unit = {
        super.updateItem(item, empty)
        if (item == null || empty) setText(null)
        else setText(item.toString)
      }
    })

    createdColumn.setPercentWidth(25)
    createdColumn
  }

  private def prepareSizeColumn: PercentageTreeTableView.PercentageTableColumn[FileTreeItemEntry, FileTreeItemEntry] = {
    val sizeColumn: PercentageTreeTableView.PercentageTableColumn[FileTreeItemEntry, FileTreeItemEntry] = new PercentageTreeTableView.PercentageTableColumn[FileTreeItemEntry, FileTreeItemEntry](Resources.SIZE)

    sizeColumn.setCellValueFactory((p: TreeTableColumn.CellDataFeatures[FileTreeItemEntry, FileTreeItemEntry]) => {
      def foo(p: TreeTableColumn.CellDataFeatures[FileTreeItemEntry, FileTreeItemEntry]): ReadOnlyObjectWrapper[FileTreeItemEntry] = if (p.getValue.getValue != null) new ReadOnlyObjectWrapper[FileTreeItemEntry](p.getValue.getValue)
      else null

      foo(p)
    })

    sizeColumn.setCellFactory((param: TreeTableColumn[FileTreeItemEntry, FileTreeItemEntry]) => new TreeTableCell[FileTreeItemEntry, FileTreeItemEntry]() {
      override protected def updateItem(item: FileTreeItemEntry, empty: Boolean): Unit = {
        super.updateItem(item, empty)
        val treeTable: TreeTableView[FileTreeItemEntry] = param.getTreeTableView
        val index: Int = getIndex

        if ((treeTable != null) && (index >= 0)) {
          val treeItem: TreeItem[FileTreeItemEntry] = treeTable.getTreeItem(index)

          if ((treeItem != null) && (treeItem.getValue.getFile != null)) if (item == null || empty || treeItem.getValue.getFile.isDirectory) setText(null)
          else setText(FileSelectView.NumberFormatter.format(item.getFile.length) + " KB")
        }
      }
    })

    sizeColumn.setComparator((f1: FileTreeItemEntry, f2: FileTreeItemEntry) => {
      val s1: Long = if (f1.getFile.isDirectory) 0
      else f1.getFile.length

      val s2: Long = if (f2.getFile.isDirectory) 0
      else f2.getFile.length

      val result: Long = s1 - s2

      if (result < 0) -1
      else if (result == 0) 0
      else 1
    })

    sizeColumn.setPercentWidth(15)
    sizeColumn
  }

  private def prepareNameColumn: PercentageTreeTableView.PercentageTableColumn[FileTreeItemEntry, String] = {
    val nameColumn: PercentageTreeTableView.PercentageTableColumn[FileTreeItemEntry, String] = new PercentageTreeTableView.PercentageTableColumn[FileTreeItemEntry, String](Resources.NAME)

    nameColumn.setCellValueFactory((p: TreeTableColumn.CellDataFeatures[FileTreeItemEntry, String]) => {
      def foo(p: TreeTableColumn.CellDataFeatures[FileTreeItemEntry, String]): ReadOnlyObjectWrapper[String] = if (p.getValue.getValue != null) new ReadOnlyObjectWrapper[String](p.getValue.getValue.getFullPath)
      else null

      foo(p)
    })

    nameColumn.setCellFactory((_: TreeTableColumn[FileTreeItemEntry, String]) => new TreeTableCell[FileTreeItemEntry, String]() {
      override protected def updateItem(itemName: String, empty: Boolean): Unit = {
        super.updateItem(itemName, empty)
        val thisTreeItem: TreeItem[FileTreeItemEntry] = this.getTreeTableRow.getTreeItem
        addMatchingTreeItemToSelectedList(itemName, thisTreeItem)

        selectionString.addListener(new ChangeListener[String] {
          override def changed(observableValue: ObservableValue[_ <: String], t: String, t1: String): Unit = {
            addMatchingTreeItemToSelectedList(itemName, thisTreeItem)
            if (thisTreeItem != null && thisTreeItem.isExpanded) {
              val children: ObservableList[TreeItem[FileTreeItemEntry]] = thisTreeItem.getChildren

              for (child <- children.asScala) {
                addMatchingTreeItemToSelectedList(child.getValue.getFullPath, child)
              }
            }
          }
        })

        setText(itemName)
      }

      private

      def addMatchingTreeItemToSelectedList(itemName: String, thisTreeItem: TreeItem[FileTreeItemEntry]): Unit = {
        if (itemName != null && thisTreeItem != null && this.getTreeTableRow.getTreeItem != null) if (selectionString.get.length > 0 && thisTreeItem.getValue.getFullPath.toLowerCase.startsWith(selectionString.get.toLowerCase)) {
          if (!selectedItems.contains(thisTreeItem)) selectedItems.add(thisTreeItem)

          setStyle("-fx-text-fill: orange;")
        }
        else setStyle("-fx-text-fill: white;")
      }
    })

    nameColumn.setPercentWidth(60)
    nameColumn
  }

  private def prepareRootFileEntry: TreeItem[FileTreeItemEntry] = {
    val item: FileTreeItemEntry = new FileTreeItemEntry("My Computer", null)
    val rootNode: TreeItem[FileTreeItemEntry] = new TreeItem[FileTreeItemEntry](item, null)
    val rootDirectories: lang.Iterable[Path] = FileSystems.getDefault.getRootDirectories

    for (name <- rootDirectories.asScala) {
      val file: File = name.toFile

      if (file != null) {
        val node = createNode(name.toAbsolutePath.toString, file, Resources.Images.IMAGE_COMPUTER)
        rootNode.getChildren.add(node)
      }
    }

    rootNode.setExpanded(true)

    rootNode
  }

  private def selectItem(): Unit = {
    val tree: TreeTableView[FileTreeItemEntry] = treeTableView.getTreeTableView
    var index: Int = tree.getSelectionModel.getSelectedIndex
    val rowSizeInPixels: Double = 22.0
    //  (Overall height - headerHeight) / one row height and get half of this
    val deltaToScrollToBack: Double = ((tree.getHeight - rowSizeInPixels) / rowSizeInPixels) / 2 - 2

    index = Math.max(0, index - deltaToScrollToBack).toInt
    tree.scrollTo(index)
  }

  private def selectPreviousItem(): Unit = {
    val tree: TreeTableView[FileTreeItemEntry] = treeTableView.getTreeTableView
    selectedItemIndex -= 1

    if (selectedItemIndex < 0) selectedItemIndex = selectedItems.size - 1

    for (i <- 0 until selectedItems.size()) {
      if (selectedItemIndex == i) {
        tree.getSelectionModel.select(selectedItems.get(i))
        selectItem()
        return
      }
    }
  }

  private def selectNextItem(): Unit = {
    val tree: TreeTableView[FileTreeItemEntry] = treeTableView.getTreeTableView

    selectedItemIndex += 1

    if (selectedItemIndex >= selectedItems.size) selectedItemIndex = 0

    for (i <- 0 until selectedItems.size()) {
      if (selectedItemIndex == i) {
        tree.getSelectionModel.select(selectedItems.get(i))
        selectItem()
        return
      }
    }
  }

  private def handleFileOpen(): Unit = {
    if (fileSelectedEvent != null) {
      val fse: FileSelectView.FileSelectEvent = new FileSelectView.FileSelectEvent(pathField.getText, fileField.getText)
      fileSelectedEvent.handle(fse)
    }
  }

  def afterCreated(): Unit = {
    treeTableView.getTreeTableView.getFocusModel.focus(0)
    treeTableView.getTreeTableView.getSelectionModel.selectFirst()
    treeTableView.getTreeTableView.requestFocus()
  }

  def setFileSelectedEvent(fileSelectedEvent: EventHandler[FileSelectView.FileSelectEvent]): Unit = {
    this.fileSelectedEvent = fileSelectedEvent
  }

  def setFileCanceledEvent(fileCanceledEvent: EventHandler[FileSelectView.FileSelectEvent]): Unit = {
    this.fileCanceledEvent = fileCanceledEvent
  }

  def initialize(defaultFileName: String, mode: FileSelectViewMode): Unit = {
    fileField.setText(defaultFileName)
  }

  private def createNode(path: String, f: File, graphicPath: String): TreeItem[FileTreeItemEntry] = {
    val graphic: Node = Utilities.createImageView(graphicPath, iconSize)

    new TreeItem[FileTreeItemEntry](new FileTreeItemEntry(path, f), graphic) {
      private var leaf: Boolean = false
      private var isFirstTimeChildren: Boolean = true
      private var isFirstTimeLeaf: Boolean = true

      override def getChildren: ObservableList[TreeItem[FileTreeItemEntry]] = {
        if (isFirstTimeChildren) {
          isFirstTimeChildren = false

          super.getChildren.setAll(buildChildren(this))
        }

        super.getChildren
      }

      override def isLeaf: Boolean = {
        if (isFirstTimeLeaf) {
          isFirstTimeLeaf = false

          val f: File = getValue.getFile
          leaf = f.isFile
        }

        leaf
      }
    }
  }

  private def buildChildren(treeItem: TreeItem[FileTreeItemEntry]): ObservableList[TreeItem[FileTreeItemEntry]] = {
    val f: File = treeItem.getValue.getFile

    if (f != null && f.isDirectory) {
      val files: Array[File] = f.listFiles

      if (files != null) {
        val children: ObservableList[TreeItem[FileTreeItemEntry]] = FXCollections.observableArrayList[TreeItem[FileTreeItemEntry]]
        var dirsList = ListBuffer[File]()
        var filesList = ListBuffer[File]()

        for (childFile <- files) {
          if (childFile.isDirectory) dirsList += childFile
          else filesList += childFile
        }

        dirsList = dirsList.sortWith((o1: File, o2: File) => o1.getName.compareTo(o2.getName) >= 0)
        filesList = filesList.sortWith((o1: File, o2: File) => o1.getName.compareTo(o2.getName) >= 0)

        //  Add all children directories
        for (file <- dirsList) {
          val node: TreeItem[FileTreeItemEntry] = createNode(file.getName, file, Resources.Images.IMAGE_FOLDER)
          val openIcon: ImageView = Utilities.createImageView(Resources.Images.IMAGE_FOLDER_OPEN, iconSize + 2)
          val closedIcon: ImageView = Utilities.createImageView(Resources.Images.IMAGE_FOLDER, iconSize)

          node.expandedProperty.addListener(new ChangeListener[lang.Boolean] {
            override def changed(observableValue: ObservableValue[_ <: lang.Boolean], t: lang.Boolean, newValue: lang.Boolean): Unit = {
              if (newValue) node.setGraphic(openIcon)
              else node.setGraphic(closedIcon)
            }
          })
          children.add(node)
        }

        //  Add all children files
        children.addAll(filesList.map(childFile => createNode(childFile.getName, childFile, Resources.Images.IMAGE_FILE)).toList.asJava)

        return children
      }
    }

    FXCollections.emptyObservableList[TreeItem[FileTreeItemEntry]]()
  }

  class FileTreeItemEntry(var fullPath: String, var file: File) {
    def getFullPath: String = fullPath

    def getFile: File = file

    def setFile(file: File): Unit = {
      this.file = file
    }
  }

}

object FileSelectView {
  private val NumberFormatter: NumberFormat = NumberFormat.getIntegerInstance

  object FileSelectViewMode extends Enumeration {
    type FileSelectViewMode = Value
    val OPEN_FILE, SAVE_FILE = Value
  }

  object FileSelectEvent {
    val FILE_SELECTED = new EventType[FileSelectEvent](Event.ANY, "FILE_SELECTED")
  }

  class FileSelectEvent(@NamedArg("eventType") eventType: EventType[_ <: Event]) extends Event(eventType) {
    private var path: String = _
    private var fileName: String = _

    def this(path: String, fileName: String) {
      this(FileSelectEvent.FILE_SELECTED)

      this.path = path
      this.fileName = fileName
    }

    def getPath: String = path

    def setPath(path: String): Unit = {
      this.path = path
    }

    def getFileName: String = fileName
  }

}
