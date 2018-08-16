// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.views

import java.io.File
import java.lang.reflect.{InvocationTargetException, Method}

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import javafx.scene.layout.{AnchorPane, BorderPane, HBox, VBox}
import javafx.util.Callback
import mill.controller.{AppController, GlobalState}
import mill.resources.ResourceHandler
import mill.{Resources, Utilities}
import org.apache.commons.collections4.keyvalue.DefaultKeyValue

import scala.collection.JavaConverters._

class NewResourceView extends BorderPane {
  private val pagination = new Pagination(2)
  private val tree = new TreeView[DefaultKeyValue[String, ResourceEntry]]()
  private var selectedEntry: DefaultKeyValue[String, ResourceEntry] = _

  private var page1: AnchorPane = _

  init()

  def init(): Unit = {
    val headerLabel = new Label(Resources.CREATE_NEW_RESOURCE)
    val topBar = new VBox(headerLabel)
    topBar.setAlignment(Pos.CENTER)
    topBar.getStyleClass.addAll("window-header")

    pagination.getStyleClass.addAll(Pagination.STYLE_CLASS_BULLET)

    AnchorPane.setTopAnchor(pagination, 1.0)
    AnchorPane.setRightAnchor(pagination, 1.0)
    AnchorPane.setBottomAnchor(pagination, 1.0)
    AnchorPane.setLeftAnchor(pagination, 1.0)

    pagination.setPageFactory(new Callback[Integer, Node] {
      override def call(param: Integer): Node = {
        if (param == 0) {
          return getPage1
        } else if (param == 1) {
          if (selectedEntry != null) {
            return getPage2
          }
          else {
            AppController.instance().showNotification(Resources.SELECT_ONE_OF_RESOURCES)

            return null
          }
        }

        null
      }
    })

    this.setTop(topBar)
    this.setCenter(pagination)
  }

  private def getPage1: AnchorPane = {
    selectedEntry = null

    if (page1 == null) {
      page1 = initializePage1
    }

    page1
  }

  private def getPage2: FileSelectView = {
    val fileSelect = AppController.instance().mainContent.getFileSelectView

    fileSelect.setFileSelectedEvent((event: FileSelectView.FileSelectEvent) => {
      getSelectedEntry.getValue.getResourceHandler.handleCreate(event.getPath, true)

      AppController.instance().switchToLastState()

      val newName = event.getPath + File.separatorChar + event.getFileName.replace(".aga", "") + File.separatorChar + event.getFileName

      GlobalState.instance().addOpenedFile(newName)
    })

    val defaultFileName = getSelectedEntry.getValue.getResourceHandler.getDefaultResourceName
    fileSelect.initialize(defaultFileName, FileSelectView.FileSelectViewMode.OPEN_FILE)

    fileSelect
  }

  private def initializePage1: AnchorPane = {
    val pane1: AnchorPane = new AnchorPane

    val searchBox: TextField = new TextField
    searchBox.setPromptText(Resources.ENTER_RESOURCE_TYPE)
    searchBox.setStyle("-fx-prompt-text-fill: rgb(120, 120, 120);")

    AnchorPane.setTopAnchor(searchBox, 0.0)

    val rootItem = new TreeItem[DefaultKeyValue[String, ResourceEntry]](ResourceEntry.createPair("", "", null))
    rootItem.setExpanded(true)

    val allClasses: List[Class[_]] = Utilities.getAllResourceClasses

    for (c <- allClasses) {
      val className: String = c.getName
      var clazz: Class[AnyRef] = null

      try {
        clazz = Class.forName(className).asInstanceOf[Class[AnyRef]]

        val o: Any = clazz.newInstance
        val resGroupName: String = "getGroupName"
        var m: Method = clazz.getDeclaredMethod(resGroupName)
        val groupName: String = m.invoke(o).asInstanceOf[String]

        if (groupName != null) {
          val resMethodName: String = "getGroupResourceName"

          m = clazz.getDeclaredMethod(resMethodName)

          val resourceName: String = m.invoke(o).asInstanceOf[String]

          if (resourceName != null) {
            var group: TreeItem[DefaultKeyValue[String, ResourceEntry]] = null
            var foundGroup: Boolean = false

            for (item: TreeItem[DefaultKeyValue[String, ResourceEntry]] <- rootItem.getChildren.asScala) {
              if (item.getValue.getKey == groupName) {
                group = item
                foundGroup = true
              }
            }

            if (!foundGroup) {
              group = createEntry(groupName, Resources.Images.IMAGE_COFFEE, resourceHandlerClass = null)
              rootItem.getChildren.add(group)
            }

            val entry: TreeItem[DefaultKeyValue[String, ResourceEntry]] = createEntry(resourceName, Resources.Images.IMAGE_COFFEE, o.asInstanceOf[ResourceHandler])
            group.setExpanded(true)
            group.getChildren.add(entry)
          }
        }
      } catch {
        case e@(_: ClassNotFoundException | _: InvocationTargetException | _: NoSuchMethodException | _: InstantiationException | _: IllegalAccessException) =>
          e.printStackTrace()
      }
    }

    tree.setRoot(rootItem)
    tree.setShowRoot(false)

    tree.setCellFactory((_: TreeView[DefaultKeyValue[String, ResourceEntry]]) => new TreeCell[DefaultKeyValue[String, ResourceEntry]]() {
      override protected def updateItem(pair: DefaultKeyValue[String, ResourceEntry], empty: Boolean): Unit = {
        super.updateItem(pair, empty)

        if (!empty && pair != null) {
          setText(pair.getKey)
          setGraphic(pair.getValue.getImageView)
        }
        else {
          setText(null)
          setGraphic(null)
        }
      }
    })

    tree.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[TreeItem[DefaultKeyValue[String, ResourceEntry]]] {
      override def changed(observableValue: ObservableValue[_ <: TreeItem[DefaultKeyValue[String, ResourceEntry]]], oldValue: TreeItem[DefaultKeyValue[String, ResourceEntry]], newValue: TreeItem[DefaultKeyValue[String, ResourceEntry]]): Unit = {
        val value: DefaultKeyValue[String, ResourceEntry] = newValue.getValue
        if (value.getValue.getResourceHandler == null) selectedEntry = null
        else selectedEntry = value
      }
    })

    tree.setOnMouseClicked((event: MouseEvent) => {
      if (event.getClickCount == 2) {
        val item: TreeItem[DefaultKeyValue[String, ResourceEntry]] = tree.getSelectionModel.getSelectedItem

        if (item.getValue.getValue.getResourceHandler != null) {
          selectedEntry = item.getValue
          pagination.setCurrentPageIndex(1)
        }
      }
    })

    tree.getStyleClass.addAll("project-explorer-no-border")

    val okButton: Button = Utilities.createButton(Resources.Images.IMAGE_ACCEPT, 20.0, Utilities.DEFAULT_IMAGE_PADDING)
    okButton.setTooltip(new Tooltip(Resources.ACCEPT))

    val cancelButton: Button = Utilities.createButton(Resources.Images.IMAGE_REJECT, 20.0, Utilities.DEFAULT_IMAGE_PADDING)
    cancelButton.setTooltip(new Tooltip(Resources.REJECT))

    okButton.setOnAction((_: ActionEvent) => {
      //  If user choose an item on resources list
      if (selectedEntry != null) pagination.setCurrentPageIndex(1)
      else AppController.instance().showNotification(Resources.SELECT_ONE_OF_RESOURCES)
    })

    cancelButton.setOnAction((_: ActionEvent) => AppController.instance().switchToLastState())

    val bottomBox: HBox = new HBox(okButton, cancelButton)
    bottomBox.setAlignment(Pos.CENTER)
    bottomBox.setSpacing(3.0)

    val bp: BorderPane = new BorderPane
    bp.setCenter(tree)
    bp.setBottom(bottomBox)

    AnchorPane.setTopAnchor(bp, 25.0)
    AnchorPane.setLeftAnchor(bp, 100.0)
    AnchorPane.setRightAnchor(bp, 100.0)
    AnchorPane.setBottomAnchor(bp, 0.0)

    pane1.widthProperty.addListener(new ChangeListener[Number] {
      override def changed(observableValue: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
        val maxWidth: Double = 100.0
        val newWidth: Double = newValue.doubleValue / 2 - maxWidth
        AnchorPane.setLeftAnchor(searchBox, newWidth)
        AnchorPane.setRightAnchor(searchBox, newWidth)
        AnchorPane.setLeftAnchor(bp, newWidth)
        AnchorPane.setRightAnchor(bp, newWidth)
      }
    })

    pane1.heightProperty.addListener(new ChangeListener[Number] {
      override def changed(observableValue: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
        val maxHeight: Double = 100.0
        val newHeight: Double = newValue.doubleValue / 2 - maxHeight
        AnchorPane.setTopAnchor(searchBox, newHeight - 30.0)
        AnchorPane.setTopAnchor(bp, newHeight)
        AnchorPane.setBottomAnchor(bp, newHeight)
      }
    })

    pane1.getChildren.addAll(searchBox, bp)

    pane1
  }

  def afterCreated(): Unit = {
    tree.getSelectionModel.selectFirst()
    tree.requestFocus()
  }

  def resetState(): Unit = {
    pagination.setCurrentPageIndex(0)
    selectedEntry = null
  }

  def keyPressed(event: KeyEvent): Unit = {
    if (event.getCode == KeyCode.ESCAPE) AppController.instance().switchToLastState()
  }

  def getSelectedEntry: DefaultKeyValue[String, ResourceEntry] = selectedEntry

  private[views] def createEntry(name: String, imageName: String, resourceHandlerClass: ResourceHandler)

  = new TreeItem[DefaultKeyValue[String, ResourceEntry]](ResourceEntry.createPair(name, imageName, resourceHandlerClass))

  object ResourceEntry {
    def createPair(title: String, image: String, resourceHandler: ResourceHandler) = new DefaultKeyValue[String, ResourceEntry](title, new ResourceEntry(image, title, resourceHandler))
  }

  class ResourceEntry(val imagePath: String, var title: String, var resourceHandler: ResourceHandler) {
    private var imageView: ImageView = _

    init()

    def init() {
      val image = new Image(Utilities.getResource(imagePath))
      imageView = new ImageView(image)

      val iconSize = 17.0
      imageView.setFitWidth(iconSize)
      imageView.setFitHeight(iconSize)
    }

    def getTitle: String = title

    def getImageView: ImageView = imageView

    def getResourceHandler: ResourceHandler = resourceHandler
  }

}
