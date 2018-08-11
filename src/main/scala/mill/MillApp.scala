// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import mill.ui.MainFrame

object MillApp {
  def main(args: Array[String]) {
    Application.launch(classOf[MillApp], args: _*)
  }
}

class MillApp extends Application {
  private val stylesURL = getClass.getResource("/app_styles.css").toExternalForm
  private val scrollbarsURL = getClass.getResource("/scrollbars.css").toExternalForm
  private val tabPaneURL = getClass.getResource("/tab_pane.css").toExternalForm
  private val codeAreaURL = getClass.getResource("/code_area.css").toExternalForm

  override def start(primaryStage: Stage) {
    val mainFrame = new MainFrame(primaryStage)
    val scene = new Scene(mainFrame, 1000, 600)
    scene.getStylesheets.addAll(stylesURL, scrollbarsURL, tabPaneURL, codeAreaURL)

    primaryStage.setTitle("mill - simple developer editor")
    primaryStage.setScene(scene)
    primaryStage.setAlwaysOnTop(true)
    primaryStage.show()
  }
}
