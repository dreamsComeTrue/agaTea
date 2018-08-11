// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import javafx.event.ActionEvent
import javafx.scene.control.{Button, Tooltip}
import javafx.scene.layout.AnchorPane

import scala.io.Source

class HeaderPane(val mainFrame: MainFrame) extends AnchorPane {
  this.setMinHeight(45)
  createNewButton()
  createOpenButton()
  createSaveButton()

  def createNewButton(): Unit = {
    val newButton = new Button()
    newButton.setId("new-button")
    newButton.setTooltip(new Tooltip("New file"))
    newButton.relocate(5, 7)

    newButton.setOnAction((_: ActionEvent) => {
      mainFrame.contentPane.addTab("new_file", "")
    })

    this.getChildren.add(newButton)
  }

  def createOpenButton(): Unit = {
    val openButton = new Button()
    openButton.setId("open-button")
    openButton.setTooltip(new Tooltip("Open file"))
    openButton.relocate(40, 7)

    openButton.setOnAction((_: ActionEvent) => {
      val file = mainFrame.openFileDialog()

      if (file != null) {
        mainFrame.contentPane.addTab(file)
      }
    })

    this.getChildren.add(openButton)
  }

  def createSaveButton(): Unit = {
    val saveButton = new Button()
    saveButton.setId("save-button")
    saveButton.setTooltip(new Tooltip("Save file"))
    saveButton.relocate(75, 7)

    saveButton.setOnAction((_: ActionEvent) => {
      val file = mainFrame.openFileDialog()

      if (file != null) {
        val text = Source.fromFile(file)

        mainFrame.contentPane.addTab(file.getName, text.mkString)
      }
    })

    this.getChildren.add(saveButton)
  }
}
