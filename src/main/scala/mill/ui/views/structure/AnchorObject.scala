// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.views.structure

import javafx.beans.property.DoubleProperty
import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.{Circle, StrokeType}

class AnchorObject(val color: Color, val x: DoubleProperty, val y: DoubleProperty, val size: Double) extends Circle(x.get, y.get, size) {
  init ()


  private def init (): Unit = {
    setFill(color.deriveColor(1, 1, 1, 0.5))
    setStroke(color)
    setStrokeWidth(2)
    setStrokeType(StrokeType.OUTSIDE)
    x.bind(centerXProperty)
    y.bind(centerYProperty)
    enableDrag()
  }

  // make a node movable by dragging it around with the mouse.
  private def enableDrag(): Unit = {
    val dragDelta = new Delta
    setOnMousePressed((mouseEvent: MouseEvent) => {
      // record a delta distance for the drag and drop operation.
      dragDelta.x = getCenterX - mouseEvent.getX
      dragDelta.y = getCenterY - mouseEvent.getY

      getScene.setCursor(Cursor.MOVE)
      mouseEvent.consume()
    })

    setOnMouseReleased((_: MouseEvent) => getScene.setCursor(Cursor.HAND))

    setOnMouseDragged((mouseEvent: MouseEvent) => {
      val newX = mouseEvent.getX + dragDelta.x

      if (newX > 0 && newX < getScene.getWidth) setCenterX(newX)

      val newY = mouseEvent.getY + dragDelta.y

      if (newY > 0 && newY < getScene.getHeight) setCenterY(newY)

      mouseEvent.consume()
    })

    setOnMouseEntered((mouseEvent: MouseEvent) => {
      if (!mouseEvent.isPrimaryButtonDown) getScene.setCursor(Cursor.HAND)
    })

    setOnMouseExited((mouseEvent: MouseEvent) => {
      if (!mouseEvent.isPrimaryButtonDown) getScene.setCursor(Cursor.DEFAULT)
    })
  }

  // records relative x and y co-ordinates.
  private class Delta {
    private[AnchorObject] var x = .0
    private[AnchorObject] var y = .0
  }

}
