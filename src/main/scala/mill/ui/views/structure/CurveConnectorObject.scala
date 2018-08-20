package mill.ui.views.structure

import javafx.beans.binding.{Bindings, DoubleBinding}
import javafx.beans.property.DoubleProperty
import javafx.scene.Group
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.{CubicCurve, Line, StrokeLineCap}

class CurveConnectorObject(val class1: ClassObject, val class2: ClassObject) extends Group {
  private var controlLine1: Line = _
  private var controlLine2: Line = _
  private var start: AnchorObject = _
  private var control1: AnchorObject = _
  private var control2: AnchorObject = _
  private var end: AnchorObject = _

  init()

  private def init(): Boolean = {
    val curve: CubicCurve = createStartingCurve(class1, class2)
    curve.setOnMouseEntered((_: MouseEvent) => curve.setStroke(Color.YELLOW))
    curve.setOnMouseExited((_: MouseEvent) => curve.setStroke(Color.WHITE))

    controlLine1 = new BoundLine(curve.controlX1Property, curve.controlY1Property, curve.startXProperty, curve.startYProperty)
    controlLine2 = new BoundLine(curve.controlX2Property, curve.controlY2Property, curve.endXProperty, curve.endYProperty)

    start = new AnchorObject(Color.PALEGREEN, curve.startXProperty, curve.startYProperty, 2)

    control1 = new AnchorObject(Color.GOLD, curve.controlX1Property, curve.controlY1Property, 5)
    control2 = new AnchorObject(Color.GOLDENROD, curve.controlX2Property, curve.controlY2Property, 5)

    end = new AnchorObject(Color.TOMATO, curve.endXProperty, curve.endYProperty, 2)

    val startXDB: DoubleBinding = Bindings.createDoubleBinding(() => {
        val c1Bounds = class1.getBounds
        val c2Bounds = class2.getBounds
        val c1x = class1.getTranslation.xProperty.doubleValue
        val c2x = class2.getTranslation.xProperty.doubleValue

        val minSize = if (c1Bounds.getWidth > c2Bounds.getWidth) c2Bounds.getWidth
        else c1Bounds.getWidth

        var result = c1x + minSize / 2
        //  C1 is TO THE LEFT c2
        if (c1x + c1Bounds.getWidth < c2x) result = c1x + c1Bounds.getWidth
        else { //  C1 is TO THE RIGHT c2
          if (c1x > c2x + c2Bounds.getWidth) result = c1x
          else {
            val delta = c1x + c1Bounds.getWidth - c2x
            val halfWidth2 = c2Bounds.getWidth * 0.5

            if (delta > halfWidth2) {
              val halfWidth1 = c1Bounds.getWidth * 0.5
              val delta2 = c1x + c1Bounds.getWidth - c2x - c2Bounds.getWidth * 0.5

              if (delta2 < halfWidth1) {
                val move = delta2 / c1Bounds.getWidth
                result = c1x + c1Bounds.getWidth - (c1Bounds.getWidth * move)
              }
              else {
                val move = delta2 / c1Bounds.getWidth
                result = c1x + c1Bounds.getWidth - (c1Bounds.getWidth * move)
              }
            }
            else result = c1x + c1Bounds.getWidth
            if (c2x + c2Bounds.getWidth * 0.5 < c1x) result = c1x
          }
        }

        if (result < end.centerXProperty.doubleValue) {
          val res = (end.getCenterX - result) * 0.5

          control1.centerXProperty.setValue(result + res)
          control2.centerXProperty.setValue(end.centerXProperty.doubleValue - res)
        }
        else {
          val res = (result - end.getCenterX) * 0.5

          control1.centerXProperty.setValue(result - res)
          control2.centerXProperty.setValue(end.centerXProperty.doubleValue + res)
        }

        result
    }, class1.getTranslation.xProperty, class2.getTranslation.xProperty)

    val startYDB: DoubleBinding = Bindings.createDoubleBinding(() => {
        val c1Bounds = class1.getLayoutBounds
        val c2Bounds = class2.getLayoutBounds
        val c1y = class1.getTranslation.yProperty.doubleValue
        val c2y = class2.getTranslation.yProperty.doubleValue
        var result = c1y + c1Bounds.getHeight / 2

        //  C1 is TO THE TOP c2
        if (c1y + c1Bounds.getHeight < c2y) result = c1y + c1Bounds.getHeight
        else { //  C1 is TO THE BOTTOM c2
          if (c1y > c2y + c2Bounds.getMaxY) result = c1y
          else {
            val delta = c1y + c1Bounds.getHeight - c2y
            val halfHeight2 = c2Bounds.getHeight * 0.5

            if (delta > halfHeight2) {
              val halfHeight1 = c1Bounds.getHeight * 0.5
              val delta2 = c1y + c1Bounds.getHeight - c2y - c2Bounds.getHeight * 0.5

              if (delta2 < halfHeight1) {
                val move = delta2 / c1Bounds.getHeight
                result = c1y + c1Bounds.getHeight - (c1Bounds.getHeight * move)
              }
              else {
                val move = delta2 / c1Bounds.getHeight
                result = c1y + c1Bounds.getHeight - (c1Bounds.getHeight * move)
              }
            }
            else result = c1y + c1Bounds.getHeight

            if (c2y + c2Bounds.getHeight * 0.5 < c1y) result = c1y
          }
        }

        result
    }, class1.getTranslation.yProperty, class2.getTranslation.yProperty)

    start.centerXProperty.bind(startXDB)
    start.centerYProperty.bind(startYDB)

    val endXDB: DoubleBinding = Bindings.createDoubleBinding(() => {
        val c1Bounds = class1.getLayoutBounds
        val c2Bounds = class2.getLayoutBounds
        val c1x = class1.getTranslation.xProperty.doubleValue
        val c2x = class2.getTranslation.xProperty.doubleValue

        val minSize = if (c1Bounds.getWidth > c2Bounds.getWidth) c2Bounds.getWidth
        else c1Bounds.getWidth

        var result = c2x + minSize / 2
        //  C2 is TO THE LEFT c1
        if (c2x + c2Bounds.getWidth < c1x) result = c2x + c2Bounds.getWidth
        else { //  C2 is TO THE RIGHT c1
          if (c2x > c1x + c1Bounds.getWidth) result = c2x
          else {
            val delta = c1x + c1Bounds.getWidth - c2x
            val halfWidth2 = c2Bounds.getWidth * 0.5

            if (delta > halfWidth2) {
              val halfWidth1 = c1Bounds.getWidth * 0.5
              val delta2 = c1x + c1Bounds.getWidth - c2x - c2Bounds.getWidth * 0.5

              if (delta2 < halfWidth1) {
                val move = delta2 / c1Bounds.getWidth
                result = c1x + c1Bounds.getWidth - (c1Bounds.getWidth * move)
              }
              else {
                val move = delta2 / c1Bounds.getWidth
                result = c1x + c1Bounds.getWidth - (c1Bounds.getWidth * move)
              }
            }
            else result = c1x + c1Bounds.getWidth
            if (c2x + c2Bounds.getWidth * 0.5 < c1x) result = c1x
          }
        }

        result
    }, class1.getTranslation.xProperty, class2.getTranslation.xProperty)

    val endYDB: DoubleBinding = Bindings.createDoubleBinding(() => {
        val c1Bounds = class1.getLayoutBounds
        val c2Bounds = class2.getLayoutBounds
        val c1y = class1.getTranslation.yProperty.doubleValue
        val c2y = class2.getTranslation.yProperty.doubleValue
        var result = c2y + c2Bounds.getHeight / 2

      //  C2 is TO THE TOP c1
        if (c2y + c2Bounds.getHeight < c1y) result = c2y + c2Bounds.getHeight
        else { //  C2 is TO THE BOTTOM c1
          if (c2y > c1y + c1Bounds.getHeight) result = c2y
          else {
            val delta = c1y + c1Bounds.getHeight - c2y
            val halfHeight2 = c2Bounds.getHeight * 0.5

            if (delta > halfHeight2) {
              val halfHeight1 = c1Bounds.getHeight * 0.5
              val delta2 = c1y + c1Bounds.getHeight - c2y - c2Bounds.getHeight * 0.5
              if (delta2 < halfHeight1) {
                val move = delta2 / c1Bounds.getHeight
                result = c1y + c1Bounds.getHeight - (c1Bounds.getHeight * move)
              }
              else {
                val move = delta2 / c1Bounds.getHeight
                result = c1y + c1Bounds.getHeight - (c1Bounds.getHeight * move)
              }
            }
            else result = c1y + c1Bounds.getHeight
            if (c2y + c2Bounds.getHeight * 0.5 < c1y) result = c1y
          }
        }

        result
    }, class1.getTranslation.yProperty, class2.getTranslation.yProperty)

    end.centerXProperty.bind(endXDB)
    end.centerYProperty.bind(endYDB)
    control1.centerYProperty.bind(start.centerYProperty)
    control2.centerYProperty.bind(end.centerYProperty)

    this.getChildren.addAll(controlLine1, controlLine2, curve, start, control1, control2, end)
  }

  private def createStartingCurve(class1: ClassObject, class2: ClassObject): CubicCurve = {
    val curve = new CubicCurve
    curve.setStroke(Color.rgb(210, 210, 210))
    curve.setStrokeWidth(2)
    curve.setStrokeLineCap(StrokeLineCap.ROUND)
    curve.setFill(Color.TRANSPARENT)
    curve
  }

  def setControlHandlesVisible(visible: Boolean): Unit = {
    controlLine1.setVisible(visible)
    controlLine2.setVisible(visible)
    start.setVisible(visible)
    control1.setVisible(visible)
    control2.setVisible(visible)
    end.setVisible(visible)
  }

  private[structure] class BoundLine private[structure](val startX: DoubleProperty, val startY: DoubleProperty, val endX: DoubleProperty, val endY: DoubleProperty) extends Line {
    startXProperty.bind(startX)
    startYProperty.bind(startY)
    endXProperty.bind(endX)
    endYProperty.bind(endY)
    setStrokeWidth(2)
    setStroke(Color.GRAY.deriveColor(0, 1, 1, 0.5))
    setStrokeLineCap(StrokeLineCap.BUTT)
    getStrokeDashArray.setAll(10.0, 5.0)
  }

}

