// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.controller.states

import javafx.scene.Node
import mill.controller.{AppController, ApplicationState}

class ApplicationStructureState extends ApplicationState {
  override def process(lastStateContent: Node): Unit = AppController.instance().mainContent.setProjectViewMode(lastStateContent, this.getContent, isProjectView = false)
}
