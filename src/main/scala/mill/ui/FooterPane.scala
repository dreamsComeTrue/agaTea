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

class FooterPane extends BorderPane {
  def changeInfoText: String => Unit = (text: String) => infoText.setText(text)

  def changedPosLabel: (Int, Int) => Unit = (column: Int, row: Int) => posLabel.setText(s"[$column:$row]")

  val infoText = new TextField("File:")
  infoText.setId("infoText")

  val dis: ObservableValue[java.lang.Boolean] = Val.map(EditorMode.mode, (sl: Number) => sl.intValue() != EditorMode.COMMAND_MODE)
  infoText.disableProperty.bind(dis)

  infoText.focusedProperty().addListener(new ChangeListener[lang.Boolean] {
    override def changed(observableValue: ObservableValue[_ <: lang.Boolean], t: lang.Boolean, t1: lang.Boolean): Unit = {
      if (t1) EditorMode.mode.set(EditorMode.COMMAND_MODE)
    }
  })
  infoText.setPadding(new Insets(1.0))
  infoText.minWidthProperty().bind(this.widthProperty().subtract(100))
  this.setLeft(infoText)

  EditorMode.mode.addListener(new ChangeListener[Number] {
    override def changed(observableValue: ObservableValue[_ <: Number], t: Number, t1: Number): Unit = {
      if (t1.intValue() == EditorMode.COMMAND_MODE) infoText.requestFocus()
    }
  })

  infoText.addEventFilter(KeyEvent.KEY_PRESSED, (event: KeyEvent) => {
    if (event.getCode == KeyCode.ENTER) {
      EditorMode.mode.set(EditorMode.NORMAL_MODE)
    }
  })

  val posLabel = new Label("[1:2]")
  posLabel.setPadding(new Insets(1.0))
  this.setRight(posLabel)
}
