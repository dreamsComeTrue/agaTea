// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill

import java.awt.Robot
import java.io.{PrintWriter, StringWriter}
import java.util

import javafx.scene.control.{Label, TextArea, _}
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.{GridPane, Priority}
import javafx.stage.StageStyle


object FxDialogs {
  def showInformation(title: String, message: String): Unit = {
    val alert = new Alert(Alert.AlertType.INFORMATION)
    alert.initStyle(StageStyle.UTILITY)
    alert.setTitle("Information")
    alert.setHeaderText(title)
    alert.setContentText(message)
    alert.getDialogPane.getStylesheets.add(getClass.getResource("/dialog_styles.css").toExternalForm)
    alert.showAndWait
  }

  def showWarning(title: String, message: String): Unit = {
    val alert = new Alert(Alert.AlertType.WARNING)
    alert.initStyle(StageStyle.UTILITY)
    alert.setTitle("Warning")
    alert.setHeaderText(title)
    alert.setContentText(message)
    alert.getDialogPane.getStylesheets.add(getClass.getResource("/dialog_styles.css").toExternalForm)
    alert.showAndWait
  }

  def showError(title: String, message: String): Unit = {
    val alert = new Alert(Alert.AlertType.ERROR)
    alert.initStyle(StageStyle.UTILITY)
    alert.setTitle("Error")
    alert.setHeaderText(title)
    alert.setContentText(message)
    alert.getDialogPane.getStylesheets.add(getClass.getResource("/dialog_styles.css").toExternalForm)
    alert.showAndWait
  }

  def showException(title: String, message: String, exception: Exception): Unit = {
    val alert = new Alert(Alert.AlertType.ERROR)
    alert.initStyle(StageStyle.UTILITY)
    alert.setTitle("Exception")
    alert.setHeaderText(title)
    alert.setContentText(message)
    val sw = new StringWriter
    val pw = new PrintWriter(sw)
    exception.printStackTrace(pw)
    val exceptionText = sw.toString
    val label = new Label("Details:")
    val textArea = new TextArea(exceptionText)
    textArea.setEditable(false)
    textArea.setWrapText(true)
    textArea.setMaxWidth(Double.MaxValue)
    textArea.setMaxHeight(Double.MaxValue)
    GridPane.setVgrow(textArea, Priority.ALWAYS)
    GridPane.setHgrow(textArea, Priority.ALWAYS)

    val expContent = new GridPane
    expContent.setMaxWidth(Double.MaxValue)
    expContent.add(label, 0, 0)
    expContent.add(textArea, 0, 1)
    alert.getDialogPane.setExpandableContent(expContent)
    alert.getDialogPane.getStylesheets.add(getClass.getResource("/dialog_styles.css").toExternalForm)
    alert.showAndWait
  }

  val YES = "Yes"
  val NO = "No"
  val OK = "OK"
  val CANCEL = "Cancel"

  def showConfirm(title: String, message: String, options: String*): String = {
    val alert = new Alert(Alert.AlertType.CONFIRMATION)
    alert.initStyle(StageStyle.UTILITY)
    alert.setTitle("Choose an option")
    alert.setHeaderText(title)
    alert.setContentText(message)
    //To make enter key press the actual focused button, not the first one. Just like pressing "space".
    alert.getDialogPane.addEventFilter(KeyEvent.KEY_PRESSED, (event: KeyEvent) => {
      if (event.getCode == KeyCode.ENTER) {
        event.consume()
        try {
          val r = new Robot
          r.keyPress(java.awt.event.KeyEvent.VK_SPACE)
          r.keyRelease(java.awt.event.KeyEvent.VK_SPACE)
        } catch {
          case e: Exception =>
            e.printStackTrace()
        }
      }
    })

    val buttons = new util.ArrayList[ButtonType]

    if (options == null || options.isEmpty) {
      buttons.add(new ButtonType(OK))
      buttons.add(new ButtonType(CANCEL))
    } else {
      for (option <- options) {
        buttons.add(new ButtonType(option))
      }
    }

    alert.getButtonTypes.setAll(buttons)

    alert.getDialogPane.getStylesheets.add(getClass.getResource("/dialog_styles.css").toExternalForm)
    val result = alert.showAndWait

    if (!result.isPresent) CANCEL else result.get.getText
  }

  def showTextInput(title: String, message: String, defaultValue: String): String = {
    val dialog = new TextInputDialog(defaultValue)
    dialog.initStyle(StageStyle.UTILITY)
    dialog.setTitle("Input")
    dialog.setHeaderText(title)
    dialog.setContentText(message)
    dialog.getDialogPane.getStylesheets.add(getClass.getResource("/dialog_styles.css").toExternalForm)

    val result = dialog.showAndWait
    if (result.isPresent) result.get else null
  }
}