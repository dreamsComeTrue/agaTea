// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import javafx.geometry.Pos
import javafx.scene.control.{Label, ListView}
import javafx.scene.layout.{StackPane, VBox}
import mill.Resources

class WindowSwitcher extends VBox {
  private var windowsList: ListView[String] = _

  init()

  private def init(): Unit = {
    val topStackPane = new StackPane(new Label(Resources.WINDOW_SWITCHER))
    topStackPane.setAlignment(Pos.CENTER)
    topStackPane.setStyle("-fx-background-color: rgb(30, 30, 30);")

    windowsList = new ListView[String]
    windowsList.getItems.addAll(Resources.PROJECT_EXPLORER_WINDOW_NAME, Resources.EDITOR_WINDOW_NAME, Resources.CONSOLE_WINDOW_NAME)
    windowsList.getStyleClass.add("file-switcher-list-view")

    val ITEM_HEIGHT = 20

    this.getChildren.addAll(topStackPane, windowsList)
    this.setVisible(false)
    this.setPrefWidth(150)
    this.setMaxWidth(150)

    val count: Int = windowsList.getItems.size + 1

    this.setPrefHeight(count * ITEM_HEIGHT)
    this.setMaxHeight(count * ITEM_HEIGHT)
  }
}
