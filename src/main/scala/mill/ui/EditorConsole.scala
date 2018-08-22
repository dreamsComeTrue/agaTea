// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import javafx.event.ActionEvent
import javafx.scene.control.{Button, TextField, ToggleButton}
import javafx.scene.input.{KeyCode, KeyEvent, MouseEvent}
import javafx.scene.layout.{BorderPane, VBox}
import javafx.scene.paint.Color
import javafx.scene.text.Font
import mill.resources.settings.ApplicationSettings
import mill.{Log, Resources, Utilities}
import org.apache.commons.lang3.StringUtils
import org.controlsfx.tools.Borders

class EditorConsole extends BorderPane {
  val output: TextEditor = createOutput()
  val inputField: TextField = createInputField()
  val buttonBar: VBox = createButtonBar()

  init()

  private def init(): Unit = {
    val consolePane = new BorderPane(output)
    consolePane.setBottom(Borders.wrap(inputField).lineBorder.innerPadding(0).outerPadding(0).color(Color.TRANSPARENT, Color.BLACK, Color.BLACK, Color.BLACK).buildAll)

    this.setMinSize(0.0, 0.0)
    this.setCenter(consolePane)
    this.setLeft(buttonBar)
  }

  private def createButtonBar(): VBox = {
    val clearConsoleButton = createClearConsoleButton
    val lockConsoleButton = createLockConsoleButton
    val stickyButton = createStickyButton

    val bar = new VBox(clearConsoleButton, lockConsoleButton, stickyButton)
    bar
  }

  private def createOutput(): TextEditor = {
    val out: TextEditor = new TextEditor("", "", "")
    out.setEditable(false)

    out
  }

  private def createInputField(): TextField = {
    val input = new TextField("> ")
    input.setStyle(" -fx-padding: 0;")
    input.setFont(Font.font(14))
    input.setOnMouseClicked((_: MouseEvent) => maintainPrompt())

    input.setOnKeyPressed((event: KeyEvent) => {
      val kc = event.getCode
      if (kc == KeyCode.ENTER) {
        var command = input.getText
        if (command.startsWith(">")) command = command.substring(1).trim
        if (StringUtils.isNotEmpty(command)) {
          Log.info("Running '" + command + "' command\n")
          input.setText("> ")
          input.positionCaret(2)
        }
      }
      else if (kc == KeyCode.UP) event.consume()
      else if (kc == KeyCode.LEFT || kc == KeyCode.BACK_SPACE || kc == KeyCode.HOME) if (input.getCaretPosition <= 2) event.consume()
      else if (kc == KeyCode.TAB && event.isShiftDown) {
        event.consume()
        output.requestFocus()
      }
      maintainPrompt()
    })

    input.setOnKeyReleased((event: KeyEvent) => {
      event.consume()
      maintainPrompt()
    })

    input
  }

  private def createClearConsoleButton: Button = {
    val clearConsoleButton = Utilities.createButton(Resources.Images.IMAGE_TRASH, 20, Utilities.DEFAULT_IMAGE_PADDING)
    clearConsoleButton.setFocusTraversable(false)
    clearConsoleButton.setOnAction((_: ActionEvent) => output.setText(""))
    clearConsoleButton
  }

  private def createLockConsoleButton: Button = {
    val lockConsoleButton = Utilities.createButton(Resources.Images.IMAGE_LOCK, 20, Utilities.DEFAULT_IMAGE_PADDING)
    lockConsoleButton.setFocusTraversable(false)
    lockConsoleButton
  }

  private def createStickyButton: ToggleButton = {
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
