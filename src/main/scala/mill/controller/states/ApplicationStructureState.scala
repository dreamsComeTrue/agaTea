// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.controller.states

import javafx.scene.Node
import mill.controller.ApplicationState
import mill.ui.MainContent

class ApplicationStructureState extends ApplicationState {
  override def process(lastStateContent: Node): Unit = MainContent.instance().setProjectViewMode(lastStateContent, this.getContent, isProjectView = false)
}
