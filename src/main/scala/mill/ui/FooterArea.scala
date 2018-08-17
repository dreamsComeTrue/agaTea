// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import java.lang

import javafx.beans.property.DoubleProperty
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.Insets
import javafx.scene.control.{Label, Slider, TextField, Tooltip}
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.BorderPane
import mill.controller.AppController
import mill.{EditorMode, Resources}
import org.reactfx.value.Val

class FooterArea private() extends BorderPane {
  private val infoText = new TextField("File:")
  private val posLabel = new Label("[1:2]")
  private var zoomSlider =  new Slider

  private val messageLabel = new Label
  private val infoLabel = new Label
  private val zoomLabel = new Label

  init()

  private def init(): Unit = {
    this.getStyleClass.add("footer-bar")

    val dis: ObservableValue[java.lang.Boolean] = Val.map(EditorMode.mode, (sl: Number) => sl.intValue() != EditorMode.COMMAND_MODE)
    infoText.disableProperty.bind(dis)

    infoText.focusedProperty().addListener(new ChangeListener[lang.Boolean] {
      override def changed(observableValue: ObservableValue[_ <: lang.Boolean], t: lang.Boolean, newValue: lang.Boolean): Unit = {
        if (newValue) EditorMode.mode.set(EditorMode.COMMAND_MODE)
      }
    })

    infoText.setPadding(new Insets(1.0))
    infoText.minWidthProperty().bind(this.widthProperty().subtract(100))

    EditorMode.mode.addListener(new ChangeListener[Number] {
      override def changed(observableValue: ObservableValue[_ <: Number], t: Number, newValue: Number): Unit = {
        if (newValue.intValue() == EditorMode.COMMAND_MODE) infoText.requestFocus()
      }
    })

    infoText.addEventFilter(KeyEvent.KEY_PRESSED, (event: KeyEvent) => {
      if (event.getCode == KeyCode.ENTER) {
        EditorMode.mode.set(EditorMode.NORMAL_MODE)
      }
    })

    posLabel.setPadding(new Insets(1.0))

    this.setLeft(infoText)
    this.setRight(posLabel)
  }

  private def prepareLabels(): Unit = {
    zoomLabel.setText("25%")
    zoomLabel.setTooltip(new Tooltip(Resources.CURRENT_ZOOM))
  }

  private def prepareZoomSlider(): Unit = {
    zoomSlider = new Slider
    zoomSlider.setFocusTraversable(false)
    zoomSlider.setMax(200)
    zoomSlider.setPrefWidth(60.0)
    zoomSlider.setTooltip(new Tooltip(Resources.ZOOM_SLIDER))
    zoomSlider.valueProperty.addListener(new ChangeListener[Number] {
      override def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
        val size = newValue.doubleValue / 10.0 + FooterArea.MIN_FONT_SIZE

        zoomLabel.setText(String.format("%2.0f", newValue.floatValue() / 2.f) + "%")

        if (AppController.instance().getActiveEditorBuffer != null) AppController.instance().getActiveEditorBuffer.getTextEditor.setFontSize(size)
      }
    })
  }

  def getZoomSliderValueProperty: DoubleProperty = zoomSlider.valueProperty

  def setInfoText(text: String): Unit = infoText.setText(text)

  def setPosLabel(column: Int, row: Int): Unit = posLabel.setText(s"[$column:$row]")
}


object FooterArea {
  val MIN_FONT_SIZE = 10.0

  private var _instance: FooterArea = _

  def instance(): FooterArea = {
    if (_instance == null) _instance = new FooterArea()

    _instance
  }
}
