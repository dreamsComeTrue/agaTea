// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import java.io.{BufferedWriter, File, FileWriter}

import javafx.event.ActionEvent
import javafx.scene.control.{Button, Tooltip}
import javafx.scene.layout.AnchorPane
import mill.FxDialogs

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
      val file = mainFrame.getFileDialog("Open file")

      if (file != null) {
        mainFrame.filePath = file.getParent
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
      val file = mainFrame.getFileDialog("Save file")

      if (file != null &&
        FxDialogs.showConfirm("Overwrite file?", "Are you sure to overwrite this file?",
          FxDialogs.YES, FxDialogs.NO).equals(FxDialogs.YES)) {
        mainFrame.filePath = file.getParent

        val text: String = mainFrame.contentPane.getCurrentTextEditor.getText
        val fileToWrite = new File(file.getCanonicalPath)
        val bw = new BufferedWriter(new FileWriter(fileToWrite))
        bw.write(text)
        bw.close()
      }
    })

    this.getChildren.add(saveButton)
  }
}
