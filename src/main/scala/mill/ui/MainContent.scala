// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import java.io.File

import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.{BorderPane, GridPane, StackPane}
import javafx.scene.{Node, Scene}
import javafx.stage.{FileChooser, Stage}
import mill.EditorMode
import mill.controller.AppController
import mill.ui.views.ProjectView

class MainContent(stage: Stage) extends BorderPane {
  private val stylesURL = getClass.getResource("/app_styles.css").toExternalForm
  private val dialogURL = getClass.getResource("/dialog_styles.css").toExternalForm
  private val splitPaneURL = getClass.getResource("/split_pane.css").toExternalForm
  private val scrollbarsURL = getClass.getResource("/scrollbars.css").toExternalForm
  private val tabPaneURL = getClass.getResource("/tab_pane.css").toExternalForm
  private val codeAreaURL = getClass.getResource("/code_area.css").toExternalForm
  private val searchBoxURL = getClass.getResource("/searchbox.css").toExternalForm

  private val styles = List(stylesURL, dialogURL, splitPaneURL, scrollbarsURL, tabPaneURL, codeAreaURL, searchBoxURL)

  private val windowSwitcher: WindowSwitcher = new WindowSwitcher
  var scene: Scene = _
  var topPane: GridPane = _
  var bottomPane: FooterPane = _
  var centerPane: StackPane = _
  var filePath: String = new File(".").getCanonicalPath

  init()

  def init(): Unit = {
    scene = new Scene(this, 1000, 600)
    scene.getStylesheets.addAll(styles: _*)

    scene.addEventFilter(KeyEvent.KEY_PRESSED,
      (event: KeyEvent) => {
        if (event.isShiftDown) {
          event.getCode match {
            case KeyCode.SEMICOLON =>
              EditorMode.mode.set(EditorMode.COMMAND_MODE)
            case _ => Unit
          }
        } else {
          event.getCode match {
            case KeyCode.ESCAPE =>
              EditorMode.mode.set(EditorMode.NORMAL_MODE)
            case _ => Unit
          }
        }
      })

    AppController.initialize(this)

    topPane = new HeaderPane(this)
    bottomPane = new FooterPane()
    centerPane = createCenterPane()
  }

  def createCenterPane(): StackPane = {
    val centerPane = new StackPane

    val projectView = ProjectView.initialize()
    projectView.setPrefWidth(Double.MaxValue)
    projectView.setPrefHeight(Double.MaxValue)

    centerPane.getChildren.addAll(projectView, windowSwitcher)
    centerPane
  }

  def setFocus(node: Node): Unit = {
    Platform.runLater(() => {
      if (!node.isFocused) {
        node.requestFocus()
        setFocus(node)
      }
    })
  }

  def assignCurrentTextEditor(textEditorOpt: Option[TextEditor]): Unit = {
    if (textEditorOpt.isDefined) {
      val textEditor = textEditorOpt.get
      val codeArea = textEditor.codeAreaVirtual.getContent

      EditorMode.mode.addListener(new ChangeListener[Number] {
        override def changed(observableValue: ObservableValue[_ <: Number], t: Number, t1: Number): Unit = {
          if (t1.intValue() == EditorMode.NORMAL_MODE) codeArea.requestFocus()
        }
      })

      setFocus(codeArea)

      def changeFunc(): Unit = {
        bottomPane.changeInfoText(if (textEditor.path != "") textEditor.path else textEditor.tabName)
        bottomPane.changedPosLabel(codeArea.getCaretColumn + 1, codeArea.getCaretSelectionBind.getParagraphIndex + 1)
      }

      changeFunc()

      codeArea.caretPositionProperty().addListener(new ChangeListener[Integer] {
        override def changed(observableValue: ObservableValue[_ <: Integer], t: Integer, t1: Integer): Unit = {
          changeFunc()
        }
      })
    } else {
      bottomPane.changeInfoText("")
      bottomPane.changedPosLabel(0, 0)
    }
  }

  def getFileDialog(title: String): File = {
    val fileChooser = new FileChooser

    fileChooser.setInitialDirectory(new File(filePath))
    fileChooser.setTitle(title)
    fileChooser.showOpenDialog(stage)
  }

  setTop(topPane)
  setBottom(bottomPane)
  setCenter(centerPane)

  assignCurrentTextEditor(None)

  //topPane.setMinHeight(1)
  //topPane.setMaxHeight(1)
}