package mill.ui.controls

import javafx.beans.property.StringProperty
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.{HBox, Region, StackPane}

class LabelSeparator(val label: String, val addText: Boolean, val topPadding: Boolean) extends StackPane {
  private var lblText: Label = _

  init()

  private def init() {
    val line = new HBox
    line.getStyleClass.addAll("line")
    line.setMinHeight(2)
    line.setPrefHeight(2)
    line.setPrefWidth(Region.USE_PREF_SIZE)
    line.setMaxHeight(Region.USE_PREF_SIZE)

    if (topPadding) setPadding(new Insets(10, 0, 0, 0))

    this.getChildren.add(line)

    if (addText) {
      lblText = new Label(label)

      this.getChildren.add(lblText)
    }

    this.getStyleClass.add("label-separator")
  }

  def this(label: String) {
    this(label, true, true)
  }

  def setText(label: String): Unit = {
    textProperty.set(label)
  }

  def getText: String = textProperty.get

  def textProperty: StringProperty = lblText.textProperty
}