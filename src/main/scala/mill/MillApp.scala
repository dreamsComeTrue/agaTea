// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.stage.Stage
import mill.ui.MainFrame

object MillApp {
  def main(args: Array[String]) {
    Application.launch(classOf[MillApp], args: _*)
  }
}

class MillApp extends Application {
  private val stylesURL = getClass.getResource("/app_styles.css").toExternalForm
  private val dialogURL = getClass.getResource("/dialog_styles.css").toExternalForm
  private val scrollbarsURL = getClass.getResource("/scrollbars.css").toExternalForm
  private val tabPaneURL = getClass.getResource("/tab_pane.css").toExternalForm
  private val codeAreaURL = getClass.getResource("/code_area.css").toExternalForm

  override def start(primaryStage: Stage) {
    val mainFrame = new MainFrame(primaryStage)
    val scene = new Scene(mainFrame, 1000, 600)
    scene.getStylesheets.addAll(stylesURL, dialogURL, scrollbarsURL, tabPaneURL, codeAreaURL)

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

    primaryStage.setTitle("mill - simple developer editor")
    primaryStage.setScene(scene)
    primaryStage.show()
  }
}
