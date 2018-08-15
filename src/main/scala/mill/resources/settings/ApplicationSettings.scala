// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.resources.settings

import javafx.beans.property.{SimpleBooleanProperty, SimpleStringProperty}

class ApplicationSettings {
  private val syntaxHighlightingEnabled = new SimpleBooleanProperty(true)
  private val lineNumbersVisible = new SimpleBooleanProperty(true)
  private val autocompletePairedChars = new SimpleBooleanProperty(true)
  private val highlightCurrentLine = new SimpleBooleanProperty(true)
  private val productiveMode = new SimpleBooleanProperty(false)

  private val stickyProjectExplorer = new SimpleBooleanProperty(true)

  private val jdkPath = new SimpleStringProperty("")
  private val vmParameters = new SimpleStringProperty("")
  private val stickyEditorConsole = new SimpleBooleanProperty(true)

  def getSyntaxHighlightingEnabled: Boolean = syntaxHighlightingEnabled.get

  def syntaxHighlightingEnabledProperty: SimpleBooleanProperty = syntaxHighlightingEnabled

  def setSyntaxHighlightingEnabled(syntaxHighlightingEnabled: Boolean): Unit = {
    this.syntaxHighlightingEnabled.set(syntaxHighlightingEnabled)
  }

  def getLineNumbersVisible: Boolean = lineNumbersVisible.get

  def lineNumbersVisibleProperty: SimpleBooleanProperty = lineNumbersVisible

  def setLineNumbersVisible(lineNumbersVisible: Boolean): Unit = {
    this.lineNumbersVisible.set(lineNumbersVisible)
  }

  def getHighlightCurrentLine: Boolean = highlightCurrentLine.get

  def highlightCurrentLineProperty: SimpleBooleanProperty = highlightCurrentLine

  def setHighlightCurrentLine(highlightCurrentLine: Boolean): Unit = {
    this.highlightCurrentLine.set(highlightCurrentLine)
  }

  def getStickyProjectExplorer: Boolean = stickyProjectExplorer.get

  def setStickyProjectExplorer(stickyProjectExplorer: Boolean): Unit = {
    this.stickyProjectExplorer.set(stickyProjectExplorer)
  }

  def stickyProjectExplorerProperty: SimpleBooleanProperty = stickyProjectExplorer

  def getStickyEditorConsole: Boolean = stickyEditorConsole.get

  def stickyEditorConsoleProperty: SimpleBooleanProperty = stickyEditorConsole

  def setStickyEditorConsole(stickyEditorConsole: Boolean): Unit = {
    this.stickyEditorConsole.set(stickyEditorConsole)
  }

  def getJdkPath: String = jdkPath.get

  def jdkPathProperty: SimpleStringProperty = jdkPath

  def setJdkPath(jdkPath: String): Unit = {
    this.jdkPath.set(jdkPath)
  }

  def getVmParameters: String = vmParameters.get

  def vmParametersProperty: SimpleStringProperty = vmParameters

  def setVmParameters(vmParameters: String): Unit = {
    this.vmParameters.set(vmParameters)
  }

  def getAutocompletePairedChars: Boolean = autocompletePairedChars.get

  def autocompletePairedCharsProperty: SimpleBooleanProperty = autocompletePairedChars

  def setAutocompletePairedChars(autocompletePairedChars: Boolean): Unit = {
    this.autocompletePairedChars.set(autocompletePairedChars)
  }

  def getProductiveMode: Boolean = productiveMode.get

  def productiveModeProperty: SimpleBooleanProperty = productiveMode

  def setProductiveMode(productiveMode: Boolean): Unit = {
    this.productiveMode.set(productiveMode)
  }
}

object ApplicationSettings {
  private var _instance: ApplicationSettings = _

  def instance(): ApplicationSettings = {
    if (_instance == null) _instance = new ApplicationSettings()

    _instance
  }
}

