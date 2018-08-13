// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill

import javafx.application.Application
import javafx.stage.Stage
import mill.ui.MainContent

object MillApp {
  def main(args: Array[String]) {
    Application.launch(classOf[MillApp], args: _*)
  }
}

class MillApp extends Application {
  override def start(primaryStage: Stage) {
    val mainContent = new MainContent(primaryStage)

    primaryStage.setTitle("mill - simple developer editor")
    primaryStage.setScene(mainContent.scene)
    primaryStage.show()
  }
}
