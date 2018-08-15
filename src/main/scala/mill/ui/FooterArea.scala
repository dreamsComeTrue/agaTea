// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import java.lang

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.geometry.Insets
import javafx.scene.control.{Label, TextField}
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.BorderPane
import mill.EditorMode
import org.reactfx.value.Val

class FooterArea private() extends BorderPane {
  private val infoText = new TextField("File:")
  private val posLabel = new Label("[1:2]")

  init()

  private def init() {
    infoText.setId("infoText")

    val dis: ObservableValue[java.lang.Boolean] = Val.map(EditorMode.mode, (sl: Number) => sl.intValue() != EditorMode.COMMAND_MODE)
    infoText.disableProperty.bind(dis)

    infoText.focusedProperty().addListener(new ChangeListener[lang.Boolean] {
      override def changed(observableValue: ObservableValue[_ <: lang.Boolean], t: lang.Boolean, newValue: lang.Boolean): Unit = {
        if (newValue) EditorMode.mode.set(EditorMode.COMMAND_MODE)
      }
    })
    infoText.setPadding(new Insets(1.0))
    infoText.minWidthProperty().bind(this.widthProperty().subtract(100))
    this.setLeft(infoText)

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

    this.setRight(posLabel)
  }

  def setInfoText(text: String): Unit = infoText.setText(text)

  def setPosLabel(column: Int, row: Int): Unit = posLabel.setText(s"[$column:$row]")
}


object FooterArea {
  private var _instance: FooterArea = _

  def instance(): FooterArea = {
    if (_instance == null) _instance = new FooterArea()

    _instance
  }
}
