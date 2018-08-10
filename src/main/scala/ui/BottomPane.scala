package ui

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane

class BottomPane extends BorderPane {
  def changeInfoLabel: String => Unit = (text: String) => infoLabel.setText(text)

  def changedPosLabel: (Int, Int) => Unit = (column: Int, row: Int) => posLabel.setText(s"[$column:$row]")

  val infoLabel = new Label("File:")
  infoLabel.setPadding(new Insets(2.0))
  this.setLeft(infoLabel)

  val posLabel = new Label("[1:2]")
  posLabel.setPadding(new Insets(2.0))
  this.setRight(posLabel)
}
