// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.controller.states

import javafx.scene.Node
import mill.controller.ApplicationState
import mill.ui.MainContent

class NewResourceState extends ApplicationState {
  private var enabled = false
  private var forceEnabled = false

  override def process(lastStateContent: Node): Unit = {
    if (!forceEnabled) enabled = !enabled

    MainContent.instance().runStatesTransition(this, this.getContent, lastStateContent, enabled)

    forceEnabled = false
  }

  def setEnabled(enabled: Boolean): Unit = {
    this.enabled = enabled
    forceEnabled = true
  }

  def isEnabled: Boolean = enabled
}
