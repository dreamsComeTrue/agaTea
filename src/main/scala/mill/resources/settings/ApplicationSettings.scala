// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.resources.settings

import javafx.beans.property.SimpleBooleanProperty

class ApplicationSettings {
  private val stickyEditorConsole = new SimpleBooleanProperty(true)

  def getStickyEditorConsole: Boolean = stickyEditorConsole.get

  def stickyEditorConsoleProperty: SimpleBooleanProperty = stickyEditorConsole

  def setStickyEditorConsole(stickyEditorConsole: Boolean): Unit = {
    this.stickyEditorConsole.set(stickyEditorConsole)
  }
}

object ApplicationSettings {
  private var _instance: ApplicationSettings = _

  def instance(): ApplicationSettings = {
    if (_instance == null) _instance = new ApplicationSettings()

    _instance
  }
}

