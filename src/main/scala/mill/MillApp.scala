// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill

import javafx.application.Application
import javafx.stage.Stage
import mill.ui.MainContent

object MillApp {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[MillApp], args: _*)
  }
}

class MillApp extends Application {
  override def start(primaryStage: Stage): Unit = {
    val mainContent = MainContent.initialize(primaryStage)

    primaryStage.setTitle("mill - simple code editor")
    primaryStage.setScene(mainContent.getScene)
    primaryStage.show()
  }
}
