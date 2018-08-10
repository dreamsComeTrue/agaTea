package ui

import java.io.File

import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.Node
import javafx.scene.layout.{AnchorPane, BorderPane}
import javafx.stage.{FileChooser, Stage}

class MainFrame(stage: Stage) extends BorderPane {
  val topPane: AnchorPane = new HeaderPane(this)
  val bottomPane = new BottomPane()
  val contentPane = new ContentPane(this)

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

      setFocus(codeArea)

      def changeFunc(): Unit = {
        bottomPane.changeInfoLabel(if (textEditor.path != "") textEditor.path else textEditor.tabName)
        bottomPane.changedPosLabel(codeArea.getCaretColumn + 1, codeArea.getCaretSelectionBind.getParagraphIndex + 1)
      }

      changeFunc()

      codeArea.caretPositionProperty().addListener(new ChangeListener[Integer] {
        override def changed(observableValue: ObservableValue[_ <: Integer], t: Integer, t1: Integer): Unit = {
          changeFunc()
        }
      })
    } else {
      bottomPane.changeInfoLabel("")
      bottomPane.changedPosLabel(0, 0)
    }
  }

  def openFileDialog(): File = {
    val fileChooser = new FileChooser

    fileChooser.setTitle("Open File")
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


