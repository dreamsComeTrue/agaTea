package mill.ui.editor

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.{ActionEvent, EventHandler}
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import mill.resources.Resource
import mill.ui.EditorArea
import mill.{Resources, Utilities}
import org.controlsfx.tools.Borders

/**
  * Created by Dominik 'squall' Jasiński on 2018-08-17.
  */
class EditorBufferHeader(var buffer: EditorBuffer) {
  private val styleNormal = "-fx-background-color: rgb(80,80,80);"
  private val styleHighlighted = "-fx-background-color: rgb(100,100,100);"
  private val styleClicked = "-fx-background-color: rgb(50,50,50);"
  private var content: Node = _
  private val closeNode = new Label("x")
  private var selected = false
  private var duringDrag: Runnable = _
  private var afterDrag: Runnable = _
  private var mouseX = .0
  private var mouseY = .0
  private var initX = .0
  private var initY = .0

  init()

  private def init() {
    selected = false

    val label = new Label(buffer.getTitle)

    val closeNodeStyleNormal = "-fx-text-fill: gray;"
    val closeNodeStyleHighlighted = "-fx-text-fill: white;"
    closeNode.setStyle(closeNodeStyleNormal)
    closeNode.setOnMouseEntered((_: MouseEvent) => closeNode.setStyle(closeNodeStyleHighlighted))
    closeNode.setOnMouseExited((_: MouseEvent) => closeNode.setStyle(closeNodeStyleNormal))

    HBox.setMargin(label, new Insets(0, 0, 0, 2))
    HBox.setMargin(closeNode, new Insets(0, 2, 0, 5))

    val hBox = new HBox(label, closeNode)
    val contextMenu: ContextMenu = buildContextMenu

    label.setContextMenu(contextMenu)

    closeNode.setContextMenu(contextMenu)

    generateContent(hBox)

    buffer.resourceProperty.addListener(new ChangeListener[Resource] {
      override def changed(observable: ObservableValue[_ <: Resource], oldValue: Resource, newValue: Resource): Unit = {
        if (newValue != null) {
          val tooltip = new Tooltip(newValue.getFullPath)
          Tooltip.install(content, tooltip)
        }
      }
    })
  }

  private def generateContent(hBox: HBox): Unit = {
    content = Borders.wrap(hBox).lineBorder.color(Color.GRAY).innerPadding(1).outerPadding(1).buildAll
    content.setStyle(styleNormal)

    content.setOnMouseEntered((_: MouseEvent) => {
      if (!selected) content.setStyle(styleHighlighted)
    })

    content.setOnMouseExited((_: MouseEvent) => {
      if (!selected) content.setStyle(styleNormal)
    })

    content.setOnMouseDragged((event: MouseEvent) => {
      if (content.getParent != null) {
        mouseX = event.getSceneX
        mouseY = event.getSceneY

        val parentBounds = content.getParent.localToScene(content.getParent.getBoundsInLocal)
        var newX = event.getSceneX - parentBounds.getMinX - initX
        newX = Math.min(Math.max(0, newX), parentBounds.getWidth - content.getLayoutBounds.getWidth)

        content.setTranslateX(newX)
        //    content.translateYProperty ().set (event.getSceneY () - parentBounds.getMinY () - initY);
        mouseX = event.getSceneX
        mouseY = event.getSceneY

        if (duringDrag != null) duringDrag.run()
      }
    })
    content.setOnMouseReleased((_: MouseEvent) => {
      if (afterDrag != null) afterDrag.run()
    })
  }

  private def buildContextMenu: ContextMenu = {
    val contextMenu = new ContextMenu

    val splitMenuItemRight = new MenuItem("Split to Right", Utilities.createImageView(Resources.Images.IMAGE_ARROW_RIGHT, 15))
    splitMenuItemRight.setOnAction((_: ActionEvent) => {
      EditorArea.instance().splitToRight(buffer)
    })

    val splitMenuItemBottom = new MenuItem("Split to Bottom", Utilities.createImageView(Resources.Images.IMAGE_ARROW_DOWN, 15))
    splitMenuItemBottom.setOnAction((_: ActionEvent) => {
      EditorArea.instance().splitToBottom(buffer)
    })

    val splitMenuItemLeft = new MenuItem("Split to Left", Utilities.createImageView(Resources.Images.IMAGE_ARROW_LEFT, 15))
    splitMenuItemLeft.setOnAction((_: ActionEvent) => {
      EditorArea.instance().splitToLeft(buffer)
    })

    val splitMenuItemTop = new MenuItem("Split to Top", Utilities.createImageView(Resources.Images.IMAGE_ARROW_UP, 15))
    splitMenuItemTop.setOnAction((_: ActionEvent) => {
      EditorArea.instance().splitToTop(buffer)
    })

    val closeThis = new MenuItem("Close", Utilities.createImageView(Resources.Images.IMAGE_CLOSE, 15))
    val closeOthers = new MenuItem("Close Others")
    val closeAll = new MenuItem("Close All")

    contextMenu.getItems.addAll(closeThis, closeOthers, closeAll, new SeparatorMenuItem, splitMenuItemRight, splitMenuItemBottom, splitMenuItemLeft, splitMenuItemTop)

    contextMenu
  }

  def getBuffer: EditorBuffer = buffer

  def getContent: Node = content

  def assignOnClick(event1: EventHandler[MouseEvent]): Unit = {
    content.setOnMousePressed((event: MouseEvent) => {
      initX = event.getX
      initY = event.getY
      content.setTranslateZ(-100)
      content.toFront()
      event1.handle(event)
    })
  }

  def assignAfterDrag(runnable: Runnable): Unit = {
    afterDrag = runnable
  }

  def setSelected(selected: Boolean): Unit = {
    this.selected = selected

    if (content != null) {
      if (selected) content.setStyle(styleClicked)
    }
    else content.setStyle(styleNormal)
  }

  def assignOnClose(event: EventHandler[MouseEvent]): Unit = {
    closeNode.setOnMouseClicked(event)
  }

  def assignDuringDrag(runnable: Runnable): Unit = {
    duringDrag = runnable
  }
}
