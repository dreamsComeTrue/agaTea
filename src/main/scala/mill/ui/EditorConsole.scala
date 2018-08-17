// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import javafx.event.ActionEvent
import javafx.scene.control.TextField
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import javafx.scene.layout.{BorderPane, VBox}
import javafx.scene.paint.Color
import javafx.scene.text.Font
import mill.resources.settings.ApplicationSettings
import mill.{Log, Resources, Utilities}
import org.apache.commons.lang3.StringUtils
import org.controlsfx.tools.Borders

class EditorConsole extends BorderPane {
  var output: TextEditor = _
  var inputField: TextField = _
  var buttonBar: VBox = _

  init()

  def init(): Unit = {
    setMinSize(0.0, 0.0)

    createButtonBar()
    createOutputControl()
    createInputField()
    createMainPane()
  }

  private def createButtonBar(): Unit = {
    val clearConsoleButton = createClearConsoleButton
    val lockConsoleButton = createLockConsoleButton
    val stickyButton = createStickyButton

    buttonBar = new VBox(clearConsoleButton, lockConsoleButton, stickyButton)
  }

  private def createOutputControl(): TextEditor = {
    output = new TextEditor("", "", "")

    output
  }

  private def createInputField(): TextField = {
    inputField = new TextField("> ")
    inputField.setStyle(" -fx-padding: 0;")
    inputField.setFont(Font.font(14))
    inputField.setOnMouseClicked((_: MouseEvent) => maintainPrompt())
    inputField.setOnKeyPressed((event: KeyEvent) => {
      val kc = event.getCode
      if (kc == KeyCode.ENTER) {
        var command = inputField.getText
        if (command.startsWith(">")) command = command.substring(1).trim
        if (StringUtils.isNotEmpty(command)) {
          Log.info("Running '" + command + "' command\n")
          inputField.setText("> ")
          inputField.positionCaret(2)
        }
      }
      else if (kc == KeyCode.UP) event.consume()
      else if (kc == KeyCode.LEFT || kc == KeyCode.BACK_SPACE || kc == KeyCode.HOME) if (inputField.getCaretPosition <= 2) event.consume()
      else if (kc == KeyCode.TAB && event.isShiftDown) {
        event.consume()
        output.requestFocus()
      }
      maintainPrompt()
    })
    inputField.setOnKeyReleased((event: KeyEvent) => {
      event.consume()
      maintainPrompt()
    })

    inputField
  }

  private def createMainPane(): Unit = {
    val consolePane = new BorderPane(output)
    consolePane.setBottom(Borders.wrap(inputField).lineBorder.innerPadding(0).outerPadding(0).color(Color.TRANSPARENT, Color.BLACK, Color.BLACK, Color.BLACK).buildAll)

    this.setCenter(consolePane)
    this.setLeft(buttonBar)
  }

  private def createClearConsoleButton = {
    val clearConsoleButton = Utilities.createButton(Resources.Images.IMAGE_TRASH, 20, Utilities.DEFAULT_IMAGE_PADDING)
    clearConsoleButton.setFocusTraversable(false)
    clearConsoleButton.setOnAction((_: ActionEvent) => output.setText(""))
    clearConsoleButton
  }

  private def createLockConsoleButton = {
    val lockConsoleButton = Utilities.createButton(Resources.Images.IMAGE_LOCK, 20, Utilities.DEFAULT_IMAGE_PADDING)
    lockConsoleButton.setFocusTraversable(false)
    lockConsoleButton
  }

  private def createStickyButton = {
    val stickyButton = Utilities.createOnOffButton(Resources.Images.MAGNET_OFF, Resources.Images.MAGNET_ON, 20, 4)
    stickyButton.setFocusTraversable(false)
    stickyButton.setOnAction((_: ActionEvent) => ApplicationSettings.instance().setStickyEditorConsole(stickyButton.isSelected))
    stickyButton.setSelected(ApplicationSettings.instance().getStickyEditorConsole)

    stickyButton
  }

  private def maintainPrompt(): Unit = {
    if (inputField.getText.length < 2) inputField.setText("> ")
    if (inputField.getCaretPosition <= 2) inputField.positionCaret(2)
  }
}
