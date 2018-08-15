// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.views.structure

import javafx.beans.binding.Bindings
import javafx.event.ActionEvent
import javafx.geometry.{Bounds, Insets}
import javafx.scene.Group
import javafx.scene.control.{Label, ToggleButton}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.MouseEvent
import javafx.scene.layout._
import javafx.scene.paint.{Color, Paint}
import javafx.scene.shape.Rectangle
import javafx.scene.text.{Text, TextFlow}
import javafx.scene.transform.{Scale, Translate}
import mill.Utilities
import mill.controller.{AppController, FlowState}
import mill.ui.views.StructureView
import mill.ui.views.structure.ClassObject.AccessModifier
import org.apache.commons.collections4.keyvalue.DefaultKeyValue

class ClassObject(var structureView: StructureView, val title: String, val width: Double, val height: Double, val color: Color, var stroke: Paint) extends Group {
  private var rect: Rectangle = _
  private val content = new AnchorPane
  private val translation = new Translate
  private var initX: Double = .0
  private var initY: Double = .0
  private var dragAnchorX: Double = .0
  private var dragAnchorY: Double = .0
  private val contentData = new VBox

  initialize(width, height, title, color, stroke)

  def initialize(width: Double, height: Double, titleText: String, color: Color, stroke: Paint): Unit = {
    this.stroke = stroke

    rect = new Rectangle(width, height)
    rect.setFill(color)
    rect.setStroke(this.stroke)

    val data: BorderPane = new BorderPane
    val title: Label = new Label("class " + titleText)
    title.getStyleClass.addAll("class-object")

    val image: Image = new Image(Utilities.getResource("coffee.png"))
    val imageView: ImageView = new ImageView(image)
    val iconSize: Double = 20.0

    imageView.setFitWidth(iconSize)
    imageView.setFitHeight(iconSize)

    val size: Double = 15.0
    val downIV: ImageView = Utilities.createImageView("arrow-down.png", size)
    val upIV: ImageView = Utilities.createImageView("arrow-up.png", size)

    val minimizeButton: ToggleButton = new ToggleButton("", upIV)
    minimizeButton.setMaxSize(size, size)
    minimizeButton.setPrefSize(size, size)
    minimizeButton.setMinSize(size, size)
    minimizeButton.setOnAction((_: ActionEvent) => {
      if (minimizeButton.isSelected) minimizeButton.setGraphic(downIV)
      else minimizeButton.setGraphic(upIV)
    })

    data.setLeft(imageView)
    data.setCenter(title)

    val buttonPane: AnchorPane = new AnchorPane(minimizeButton)
    AnchorPane.setRightAnchor(minimizeButton, -2.0)
    AnchorPane.setLeftAnchor(minimizeButton, 2.0)
    AnchorPane.setTopAnchor(minimizeButton, 3.0)

    //buttonPane.setPrefWidth (100);
    data.setRight(buttonPane)

    val borderPane: BorderPane = new BorderPane
    StackPane.setMargin(borderPane, new Insets(0, 4, 0, 4))
    borderPane.setTop(data)

    borderPane.setCenter(contentData)

    content.getChildren.addAll(rect, borderPane)

    AnchorPane.setLeftAnchor(borderPane, 5.0)

    this.getChildren.addAll(content)
    rect.widthProperty.bind(Bindings.add(10, borderPane.widthProperty))
    //	rect.heightProperty ().bind (content.heightProperty ());
    this.setAutoSizeChildren(true)

    this.getTransforms.addAll(translation)
    this.setOnMouseDragged((event: MouseEvent) => {
      val scale: Scale = structureView.getCameraScale
      val dragX: Double = event.getSceneX / scale.getX - dragAnchorX
      val dragY: Double = event.getSceneY / scale.getY - dragAnchorY
      val newXPosition: Double = initX + dragX
      val newYPosition: Double = initY + dragY
      translation.setX(newXPosition)
      translation.setY(newYPosition)
      event.consume()
      setStroke(Color.CORNFLOWERBLUE)
    })

    this.setOnMousePressed((event: MouseEvent) => {
      val scale: Scale = structureView.getCameraScale
      //when mouse is pressed, store initial position
      initX = translation.getX
      initY = translation.getY
      dragAnchorX = event.getSceneX / scale.getX
      dragAnchorY = event.getSceneY / scale.getY

      this.toFront()

      event.consume()
      setStroke(Color.BLUE)
    })

    this.setOnMouseClicked((event: MouseEvent) => {
      if (event.getClickCount > 1) AppController.instance().setFlowState(FlowState.APPLICATION_PROJECT)
    })

    this.setOnMouseReleased((_: MouseEvent) => setStroke(stroke))
    this.setOnMouseEntered((_: MouseEvent) => rect.setStroke(Color.YELLOW))
    this.setOnMouseMoved((_: MouseEvent) => rect.setStroke(Color.YELLOW))
    this.setOnMouseExited((_: MouseEvent) => rect.setStroke(this.stroke))
  }

  private def setStroke(paint: Paint): Unit = {
    rect.setStroke(paint)
  }

  def setTranslation(x: Double, y: Double, z: Double): Unit = {
    translation.setX(x)
    translation.setY(y)
    translation.setZ(z)
  }

  def addMethod(methodName: String, modifier: AccessModifier, returnType: String, parameters: List[DefaultKeyValue[String, String]]): MethodSlot = {
    val methodSlot: MethodSlot = new MethodSlot(methodName, modifier, returnType, parameters)

    contentData.getChildren.addAll(methodSlot)

    methodSlot
  }

  def getTranslation: Translate = translation

  def getBounds: Bounds = content.getLayoutBounds

  class MethodSlot(val methodName: String, val modifier: AccessModifier, val returnType: String, val parameters: List[DefaultKeyValue[String, String]]) extends TextFlow {
    init()

    private def init() {
      var t: Text = new Text(modifier.name + " ")
      t.getStyleClass.add("method-slot-name")

      this.getChildren.add(t)

      t = new Text(returnType + " ")
      t.getStyleClass.add("method-slot-type")

      this.getChildren.add(t)

      if (parameters.nonEmpty) {
        t = new Text(methodName + " (")
        t.getStyleClass.add("method-slot-name")

        this.getChildren.add(t)

        var counter = 0
        for (entry <- parameters) {
          counter += 1

          t = new Text(entry.getKey + " ")
          t.getStyleClass.add("method-slot-type")

          this.getChildren.add(t)

          t = new Text(entry.getValue)
          t.getStyleClass.add("method-slot-identifier")

          this.getChildren.add(t)

          if (counter < parameters.size - 1) {
            t = new Text(", ")
            t.getStyleClass.add("method-slot-name")
            this.getChildren.add(t)
          }
        }

        t = new Text(");")
        t.getStyleClass.add("method-slot-name")

        this.getChildren.add(t)
      }
      else {
        t = new Text(methodName + " ();")
        t.getStyleClass.add("method-slot-name")

        this.getChildren.add(t)
      }

      this.setOnMouseEntered((_: MouseEvent) => this.setStyle("-fx-border-color: wheat;"))
      this.setOnMouseExited((_: MouseEvent) => this.setStyle("-fx-border-color: transparent;"))
    }
  }

}

object ClassObject {

  sealed trait AccessModifier {
    def name: String
  }

  case object EMPTY extends AccessModifier {
    val name = ""
  }

  case object PRIVATE extends AccessModifier {
    val name = "private"
  }

  case object PUBLIC extends AccessModifier {
    val name = "public"
  }

  case object PROTECTED extends AccessModifier {
    val name = "protected"
  }

  case class UnknownCurrency(name: String) extends AccessModifier

}
