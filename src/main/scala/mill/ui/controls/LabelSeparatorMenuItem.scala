package mill.ui.controls

import javafx.scene.control.SeparatorMenuItem
import javafx.scene.layout.Region

class LabelSeparatorMenuItem(val label: String, val addText: Boolean, val topPadding: Boolean) extends SeparatorMenuItem {
  init()

  private def init() {
    val content = new LabelSeparator(label, addText, topPadding)
    content.setPrefHeight(Region.USE_COMPUTED_SIZE)
    content.setMinHeight(Region.USE_PREF_SIZE)

    setContent(content)
  }

  def this(label: String) {
    this(label, true, true)
  }
}
