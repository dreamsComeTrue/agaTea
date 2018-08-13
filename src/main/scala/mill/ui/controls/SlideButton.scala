// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.controls

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.image.ImageView
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.stage.{Popup, PopupWindow}

class SlideButton(val s: String, val imageView: ImageView, val buttonSize: Double) extends Button(s, imageView) {
  private val pop: Popup = new Popup
  private var horizontalOrientation = true
  private var isPrimaryButtonAction = false
  private var alignPos: Pos = Pos.BOTTOM_RIGHT

  init()

  def init() {
    pop.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_TOP_LEFT)

    setMaxSize(buttonSize, buttonSize)
    setPrefSize(buttonSize, buttonSize)
    setMinSize(buttonSize, buttonSize)

    this.setOnMouseClicked((event: MouseEvent) => {
      var button = MouseButton.SECONDARY

      if (isPrimaryButtonAction) button = MouseButton.PRIMARY

      if (event.getButton.compareTo(button) == 0) if (pop.isShowing) pop.hide()
      else {
        val scene = this.getScene
        val nodeCoord = this.localToScene(0.0, 0.0)
        val clickX = scene.getWindow.getX + scene.getX + nodeCoord.getX.round
        val clickY = scene.getWindow.getY + scene.getY + nodeCoord.getY.round
        var contentWidth = pop.getContent.get(0).prefWidth(-1)

        if (horizontalOrientation) alignPos match {
          case Pos.BOTTOM_RIGHT => pop.show(this, clickX + this.getWidth, clickY - 1)
          case Pos.BOTTOM_CENTER => pop.show(this, clickX - this.getWidth * 0.5, clickY - 1)
          case _ => pop.show(this, clickX + this.getWidth, clickY - 1)
        }
        else alignPos match {
          case Pos.BOTTOM_RIGHT => pop.show(this, clickX - 1, clickY + this.getHeight)
          case Pos.BOTTOM_CENTER => {
            pop.show(this, clickX - 1 - contentWidth * 0.5 + this.getWidth, clickY + this.getHeight)
            contentWidth = pop.getContent.get(0).prefWidth(-1)
            pop.setAnchorX(clickX - 1 - contentWidth * 0.5 + this.getWidth)
          }
          case _ => pop.show(this, clickX - 1, clickY + this.getHeight)
        }
      }
    })

    this.setOnMouseExited((event: MouseEvent) => {
      if (horizontalOrientation) if (event.getX < this.getWidth) pop.hide()
      else if (event.getY < this.getHeight) pop.hide()
    })
  }

  def setContent(content: Node): Unit = {
    pop.getContent.clear()
    pop.getContent.addAll(content)

    content.setOnMouseExited((event: MouseEvent) => {
      if (horizontalOrientation) if (event.getX >= this.getWidth) pop.hide()
      else if (event.getY >= 0) pop.hide()
    })
  }

  def setAlignPos(alignPos: Pos): Unit = {
    this.alignPos = alignPos
  }

  def setHorizontalOrientation(horizontal: Boolean): Unit = {
    this.horizontalOrientation = horizontal
  }

  def hide(): Unit = {
    pop.hide()
  }

  def setPrimaryButtonAction(isPrimaryButtonAction: Boolean): Unit = {
    this.isPrimaryButtonAction = isPrimaryButtonAction
  }
}
