// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import java.io.File

import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.Node
import javafx.scene.layout.{AnchorPane, BorderPane}
import javafx.stage.{FileChooser, Stage}
import mill.EditorMode

class MainFrame(stage: Stage) extends BorderPane {
  val topPane: AnchorPane = new HeaderPane(this)
  val bottomPane = new FooterPane()
  val contentPane = new ContentPane(this)
  var filePath: String = new File(".").getCanonicalPath

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
  setCenter(contentPane)

  assignCurrentTextEditor(None)

  contentPane.addTab("new_file", "")

  //topPane.setMinHeight(1)
  //topPane.setMaxHeight(1)
}


