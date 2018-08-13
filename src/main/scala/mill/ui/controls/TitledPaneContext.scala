// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.controls

import com.sun.javafx.scene.control.skin.TitledPaneSkin
import javafx.scene.Node
import javafx.scene.control.{ContextMenu, TitledPane}
import javafx.scene.input.ContextMenuEvent
import mill.controller.FXStageInitializer

class TitledPaneContext(val title: String, val content: Node) extends TitledPane(title, content) with FXStageInitializer {
  var contextMenu: ContextMenu = null

  override def fxInitialize: Boolean = {
    if ((getChildren != null) && (getChildren.size > 0)) {
      val node = getChildren.get(1)

      if (node != null) {
        if (contextMenu != null) {
          node.setOnContextMenuRequested((event: ContextMenuEvent) => contextMenu.show(node, event.getScreenX, event.getScreenY))
        }

        return true
      }
    }

    false
  }

  def getTitleRegion: Node = {
    val skin = getSkin.asInstanceOf[TitledPaneSkin]
    skin.getChildren.get(1)
  }
}
