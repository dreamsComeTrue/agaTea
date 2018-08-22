// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import java.lang

import javafx.beans.property.DoubleProperty
import javafx.geometry.Insets
import javafx.scene.control.{Label, Slider, TextField, Tooltip}
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.{BorderPane, HBox}
import mill.controller.AppController
import mill.{EditorMode, Resources}
import org.reactfx.value.Val

class FooterArea private() extends BorderPane {
  private val infoText = prepareInfoText()
  private val posLabel = preparePosLabel()
  private val zoomSlider = prepareZoomSlider()
  private val zoomLabel = prepareLabels()

  init()

  private def init(): Unit = {
    EditorMode.mode.addListener((_, _: Number, newValue: Number) => if (newValue.intValue() == EditorMode.COMMAND_MODE) infoText.requestFocus())

    this.getStyleClass.add("footer-bar")
    this.setCenter(infoText)
    this.setRight(new HBox(zoomSlider, zoomLabel, posLabel))
  }

  private def preparePosLabel(): Label = {
    val label = new Label("[1:2]")
    label.setPadding(new Insets(1.0))

    label
  }

  private def prepareInfoText(): TextField = {
    val info = new TextField("File:")
    info.disableProperty.bind(Val.map(EditorMode.mode, (sl: Number) => sl.intValue() != EditorMode.COMMAND_MODE))

    info.focusedProperty().addListener((_, _: lang.Boolean, newValue: lang.Boolean) => {
      if (newValue) EditorMode.mode.set(EditorMode.COMMAND_MODE)
    })

    info.addEventFilter(KeyEvent.KEY_PRESSED, (event: KeyEvent) => {
      if (event.getCode == KeyCode.ENTER) {
        EditorMode.mode.set(EditorMode.NORMAL_MODE)
      }
    })

    info.setPadding(new Insets(1.0))
    info.minWidthProperty().bind(this.widthProperty().subtract(300))

    info
  }

  private def prepareLabels(): Label = {
    val label = new Label()
    label.setText("0%")
    label.setMinWidth(40)
    label.setTooltip(new Tooltip(Resources.CURRENT_ZOOM))

    HBox.setMargin(label, new Insets(0, 0, 0, 5))
    label
  }

  private def prepareZoomSlider(): Slider = {
    val slider = new Slider
    slider.setFocusTraversable(false)
    slider.setMax(200)
    slider.setPrefWidth(60.0)
    slider.setTooltip(new Tooltip(Resources.ZOOM_SLIDER))

    slider.valueProperty.addListener((_, _: Number, newValue: Number) => {
      val size = newValue.doubleValue / 10.0 + FooterArea.MIN_FONT_SIZE

      val newVal: java.lang.Float = newValue.floatValue() / 2.0f
      zoomLabel.setText(String.format("%2.0f", newVal) + "%")

      if (AppController.instance().getActiveEditorBuffer != null) AppController.instance().getActiveEditorBuffer.getTextEditor.setFontSize(size)
    })

    slider
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
