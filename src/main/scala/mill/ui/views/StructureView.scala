// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.views

import javafx.beans.binding.Bindings
import javafx.scene.input.{MouseEvent, ScrollEvent}
import javafx.scene.layout.{BorderPane, StackPane, VBox}
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.transform.{Scale, Translate}
import javafx.scene.{Group, Node, PerspectiveCamera, Scene}
import mill.ui.MainContent
import mill.ui.views.structure.{ClassObject, CurveConnectorObject}
import mill.{Resources, Utilities}
import org.apache.commons.collections4.keyvalue.DefaultKeyValue
import org.controlsfx.tools.Borders

import scala.collection.mutable.ListBuffer

class StructureView private(val container: StackPane, scene: Scene) extends BorderPane {
  private var camera: PerspectiveCamera = _
  private var cameraTranslate: Translate = _
  private var cameraScale: Scale = _

  private var initX = .0
  private var initY = .0
  private var dragAnchorX = .0
  private var dragAnchorY = .0

  private var root: Group = _
  private var centerStack: StackPane = _
  private var centerPaneBorder: Node = _

  init()

  private def init(): Unit = {
    val clipRect = new Rectangle(0, 0, container.getWidth, container.getHeight)

    clipRect.widthProperty.bind(Bindings.subtract(container.widthProperty, 4))
    clipRect.heightProperty.bind(Bindings.subtract(container.heightProperty, 2))

    scene.setCamera(camera)

    root = new Group
    root.setManaged(false)
    root.setAutoSizeChildren(false)

    cameraTranslate = new Translate
    cameraScale = new Scale
    root.getTransforms.addAll(cameraTranslate, cameraScale)

    root.getChildren.addAll(create3dContent(): _*)

    val stackPane = new StackPane
    stackPane.getChildren.addAll(root)
    stackPane.setClip(clipRect)

    container.setOnMouseDragged((event: MouseEvent) => {
      val dragX = event.getSceneX - dragAnchorX
      val dragY = event.getSceneY - dragAnchorY
      val newXPosition = initX + dragX
      val newYPosition = initY + dragY

      cameraTranslate.setX(newXPosition)
      cameraTranslate.setY(newYPosition)
    })

    container.setOnMousePressed((event: MouseEvent) => {
      //when mouse is pressed, store initial position
      initX = cameraTranslate.getX
      initY = cameraTranslate.getY
      dragAnchorX = event.getSceneX
      dragAnchorY = event.getSceneY
    })

    container.setOnScroll((event: ScrollEvent) => {
      var newX = cameraScale.getX + event.getDeltaY / 1000

      if (newX > 1) newX = 1
      else if (newX < 0.05) newX = 0.05

      var newY = cameraScale.getY + event.getDeltaY / 1000

      if (newY > 1) newY = 1
      else if (newY < 0.05) newY = 0.05

      cameraScale.setX(newX)
      cameraScale.setY(newY)

      val width = root.getLayoutBounds.getWidth * 0.5
      val height = root.getLayoutBounds.getHeight * 0.5

      cameraScale.setPivotX(width) // - event.getX () * (1 - newX));
      cameraScale.setPivotY(height)
    })

    val toolboxBar = createToolbar()

    centerPaneBorder = Borders.wrap(stackPane).lineBorder.outerPadding(0).innerPadding(0).color(Color.rgb(50, 50, 50), Color.TRANSPARENT, Color.TRANSPARENT, Color.rgb(50, 50, 50)).buildAll
    centerStack = new StackPane
    centerStack.getChildren.addAll(centerPaneBorder)

    this.setLeft(Borders.wrap(toolboxBar).lineBorder.outerPadding(0).innerPadding(0).color(Color.rgb(50, 50, 50), Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT).buildAll)
    this.setCenter(centerStack)
  }

  private def createToolbar(): VBox = {
    val cutButton = Utilities.createButton(Resources.Images.IMAGE_CUT, 20, Utilities.DEFAULT_IMAGE_PADDING)
    cutButton.setFocusTraversable(false)

    new VBox(cutButton)
  }

  def setVisibility(visible: Boolean): Unit = {
    val content = MainContent.instance()

    if (visible) {
      content.getScene.setCamera(camera)
      this.setVisible(true)
    }
    else {
      content.getScene.setCamera(null)
      this.setVisible(false)
    }
  }

  def create3dContent(): List[Node] = {
    val c1: ClassObject = new ClassObject(this, "Klasa1", 150, 50, Color.rgb(120, 120, 120), Color.rgb(50, 50, 50))
    val c2: ClassObject = new ClassObject(this, "Klasa2", 150, 150, Color.rgb(120, 120, 120), Color.rgb(50, 50, 50))
    val c3: ClassObject = new ClassObject(this, "Klasa3", 150, 250, Color.rgb(120, 120, 120), Color.rgb(50, 50, 50))
    val cc1: CurveConnectorObject = new CurveConnectorObject(c1, c2)
    val cc2: CurveConnectorObject = new CurveConnectorObject(c3, c2)
    val cc3: CurveConnectorObject = new CurveConnectorObject(c1, c3)

    c1.setTranslation(0, 0, 0)
    c2.setTranslation(400, 350, 0)
    c3.setTranslation(700, 50, 0)
    cc1.setControlHandlesVisible(false)
    cc2.setControlHandlesVisible(false)
    cc3.setControlHandlesVisible(false)

    val pars = ListBuffer[DefaultKeyValue[String, String]]()
    pars += new DefaultKeyValue[String, String]("String", "name")
    c1.addMethod("method1", ClassObject.PRIVATE, "void", pars.toList)

    pars += new DefaultKeyValue[String, String]("int", "age")
    c3.addMethod("method1", ClassObject.PROTECTED, "void", pars.toList)

    List[Node](c1, c2, c3, cc1, cc2, cc3)
  }

  def getCameraScale: Scale = cameraScale

  def getRootNode: Node = root

  def showView(content: Node): Unit = {
    centerStack.getChildren.clear()

    if (content != null) centerStack.getChildren.add(content)

    centerStack.getChildren.add(centerPaneBorder)
  }

}


object StructureView {
  private var _instance: StructureView = _

  def initialize(container: StackPane, scene: Scene): StructureView = {
    if (_instance == null) _instance = new StructureView(container, scene)

    _instance
  }

  def instance(): StructureView = _instance
}
