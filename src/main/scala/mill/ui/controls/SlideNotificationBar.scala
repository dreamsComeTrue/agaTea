// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.controls

import javafx.animation.{Interpolator, KeyFrame, KeyValue, Timeline}
import javafx.beans.property.{DoubleProperty, SimpleDoubleProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.ActionEvent
import javafx.geometry.{Insets, Pos}
import javafx.scene.control.{Control, Label}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import javafx.scene.layout._
import javafx.scene.text.Font
import javafx.scene.{Node, Parent}
import javafx.util.Duration
import mill.{Resources, Utilities}

import scala.collection.JavaConverters._

object SlideNotificationBar {
  val DEFAULT_HEIGHT = 50.0
}

class SlideNotificationBar(val wrappedContent: Node, val ownContent: Node) extends Control {
  var transition: DoubleProperty = new SimpleDoubleProperty() {
    override protected def invalidated(): Unit = {
      layoutChildren()
    }
  }

  private val pane = new GridPane
  private var initialFocusNode: Node = _
  private var label = new Label
  private var maxHeight = SlideNotificationBar.DEFAULT_HEIGHT
  private var iconView: ImageView = _
  private var tl: Timeline = _
  private var kf: KeyFrame = _
  private var kf2: KeyFrame = _
  private var kf3: KeyFrame = _
  private var kf4: KeyFrame = _
  private var easeTime = .0
  private var showDuration = .0
  private var isHidden = false

  private[controls] val focusListener = new ChangeListener[java.lang.Boolean]() {
    override def changed(observable: ObservableValue[_ <: java.lang.Boolean], oldValue: java.lang.Boolean, newValue: java.lang.Boolean): Unit = {
      if (!newValue && !isChildFocused(pane)) hide()
    }
  }

  init()

  private def init(): Unit = {
    var ownContent1: Node = null

    if (ownContent == null) {
      label = new Label
      ownContent1 = label
    }
    else {
      ownContent1 = ownContent
    }

    pane.add(ownContent1, 1, 0)

    iconView = new ImageView(new Image(Utilities.getResource(Resources.Images.IMAGE_WARNING)))
    iconView.setFitWidth(40)
    iconView.setFitHeight(40)

    GridPane.setMargin(iconView, new Insets(0, 0, 0, 10.0))
    pane.add(iconView, 0, 0)
    pane.setAlignment(Pos.CENTER_LEFT)
    //  pane.setGridLinesVisible (true);

    val column1 = new ColumnConstraints(50)
    val column2 = new ColumnConstraints(100, 100, Double.MaxValue)
    column2.setHgrow(Priority.ALWAYS)

    pane.getColumnConstraints.addAll(column1, column2)
    pane.setPrefHeight(0.0)
    pane.setMinHeight(0.0)
    pane.setMaxHeight(0.0)
    pane.prefWidthProperty.bind(this.widthProperty)
    pane.maxWidthProperty.bind(this.maxWidthProperty)

    StackPane.setAlignment(pane, Pos.TOP_CENTER)
    getChildren.addAll(new StackPane(wrappedContent, pane))

    pane.getStyleClass.add("slide-notification-bar")
    pane.opacityProperty.bind(transition)
    pane.setVisible(false)
    pane.setDisable(true)

    enableOnClickDisposal(true)

    pane.focusedProperty.addListener(focusListener)
    pane.setOnKeyPressed((event: KeyEvent) => {
      if (event.getCode eq KeyCode.ESCAPE) hide()
    })
  }

  def this(wrappedContent: Node, text: String, fontSize: Int) {
    this(wrappedContent, null)
    setText(text, fontSize)
  }

  private def isChildFocused(parent: Parent): Boolean = {
    for (child <- parent.getChildrenUnmodifiable.asScala) {
      if (child.isFocused) {
        return true
      }
      else child match {
        case parent1: Parent =>
          if (isChildFocused(parent1)) return true
        case _ =>
      }
    }

    false
  }

  private def injectFocusToAllChildren(parent: Parent): Unit = {
    for (child <- parent.getChildrenUnmodifiable.asScala) {
      child.focusedProperty.addListener(focusListener)

      child match {
        case parent1: Parent => injectFocusToAllChildren(parent1)
        case _ =>
      }
    }
  }

  def enableOnClickDisposal(enable: Boolean): Unit = {
    if (enable) {
      pane.setOnMouseClicked((_: MouseEvent) => hide())
    }
    else {
      pane.setOnMouseClicked((_: MouseEvent) => {
      })
    }
  }

  def setOwnContent(content: Pane): Unit = {
    if (content != null) {
      pane.getChildren.set(0, content)

      content.prefWidthProperty.bind(pane.widthProperty)
      content.minWidthProperty.bind(pane.widthProperty)

      injectFocusToAllChildren(pane)
    }
    else pane.getChildren.set(0, label)
  }

  override protected def createDefaultSkin = new SlideNotificationBarSkin(this)

  override protected def layoutChildren(): Unit = {
    super.layoutChildren()

    pane.setPrefHeight(maxHeight * transition.getValue)
    pane.setMaxHeight(maxHeight * transition.getValue)
  }

  def setBarHeight(height: Double): Unit = {
    maxHeight = height
  }

  def setText(text: String, fontSize: Int): Unit = {
    label.setText(text)
    label.setFont(Font.font(fontSize))
  }

  def show(easeTime: Double, showDuration: Double, closeImmediately: Boolean): Unit = {
    this.showDuration = showDuration
    this.easeTime = easeTime

    pane.setVisible(true)
    pane.setDisable(false)

    tl = new Timeline
    tl.setCycleCount(1)

    kf = new KeyFrame(Duration.ZERO, (_: ActionEvent) => {
    }, new KeyValue(transition, java.lang.Double.valueOf(0.0)))

    kf2 = new KeyFrame(Duration.millis(easeTime), (_: ActionEvent) => {
    }, new KeyValue(transition, java.lang.Double.valueOf(1.0), Interpolator.EASE_OUT))

    kf3 = new KeyFrame(Duration.millis(showDuration + easeTime), (_: ActionEvent) => {
    }, new KeyValue(transition, java.lang.Double.valueOf(1.0)))

    tl.getKeyFrames.setAll(kf, kf2, kf3)

    if (closeImmediately) {
      kf4 = new KeyFrame(Duration.millis(showDuration + easeTime + easeTime), (_: ActionEvent) => {
      }, new KeyValue(transition, java.lang.Double.valueOf(0.0), Interpolator.EASE_IN))

      tl.getKeyFrames.add(kf4)
      tl.onFinishedProperty.set((_: ActionEvent) => {
        pane.setVisible(false)
        pane.setDisable(true)
      })
    }

    tl.playFromStart()

    if (initialFocusNode != null) initialFocusNode.requestFocus()

    isHidden = false
  }

  def hide(): Unit = {
    if (!isHidden) {
      kf3 = new KeyFrame(Duration.ZERO, (_: ActionEvent) => {
      }, new KeyValue(transition, java.lang.Double.valueOf(1.0)))

      kf4 = new KeyFrame(Duration.millis(easeTime), (_: ActionEvent) => {
      }, new KeyValue(transition, java.lang.Double.valueOf(0.0), Interpolator.EASE_IN))

      tl.getKeyFrames.setAll(kf3, kf4)
      tl.playFromStart()
      tl.onFinishedProperty.set((_: ActionEvent) => {
        isHidden = true
        pane.setVisible(false)
        pane.setDisable(true)
      })
    }
  }

  def setInitialFocusNode(initialFocus: Node): Unit = {
    initialFocusNode = initialFocus
  }

  def showIcon(iconName: String): Unit = {
    if (iconName == null) iconView.setVisible(false)
    else {
      iconView.setImage(new Image(Utilities.getResource(iconName)))
      iconView.setVisible(true)
    }
  }
}

