// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.views

import java.io.File

import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.{BorderPane, VBox}
import mill.Resources
import mill.controller.AppController
import mill.resources.ResourceFactory

class OpenResourceView extends BorderPane {
  init()

  private def init(): Unit = {
    val headerLabel = new Label(Resources.OPEN_RESOURCE)

    val topBar = new VBox(headerLabel)
    topBar.setAlignment(Pos.CENTER)
    topBar.getStyleClass.addAll("window-header")

    val fileSelectView = AppController.instance().mainContent.getFileSelectView

    this.setTop(topBar)
    this.setCenter(fileSelectView)
  }

  def refreshFileSelectView(): Unit = {
    val fileSelectView = AppController.instance().mainContent.getFileSelectView
    fileSelectView.setFileSelectedEvent((event: FileSelectView.FileSelectEvent) => {
      val newName = event.getPath + File.separatorChar + event.getFileName
      val res = ResourceFactory.handleFileOpen(newName)

      if (res != null) AppController.instance().switchToLastState()
    })

    this.setCenter(fileSelectView)
  }

  def keyPressed(event: KeyEvent): Unit = {
    if (event.getCode == KeyCode.ESCAPE) AppController.instance().switchToLastState()
  }
}
