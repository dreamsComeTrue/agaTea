// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.views

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.{BorderPane, VBox}
import mill.Resources
import mill.controller.AppController
import mill.resources.ResourceFactory
import mill.ui.MainContent

class OpenResourceView private() extends BorderPane {
  init()

  private def init(): Unit = {
    val headerLabel = new Label(Resources.OPEN_RESOURCE)

    val topBar = new VBox(headerLabel)
    topBar.setAlignment(Pos.CENTER)
    topBar.getStyleClass.addAll("window-header")

    this.setTop(topBar)
    this.setCenter(FileSelectView.instance())
  }

  def refreshFileSelectView(): Unit = {
    FileSelectView.instance().setFileSelectedEvent((event: FileSelectView.FileSelectEvent) => {
      val res = ResourceFactory.handleFileOpen(event.getPath)

      if (res != null) AppController.instance().switchToLastState()
    })

    this.setCenter(FileSelectView.instance())
  }

  def keyPressed(event: KeyEvent): Unit = {
    if (event.getCode == KeyCode.ESCAPE) AppController.instance().switchToLastState()
  }
}

object OpenResourceView {
  private var _instance: OpenResourceView = _

  def instance(): OpenResourceView = {
    if (_instance == null) _instance = new OpenResourceView()

    _instance
  }
}