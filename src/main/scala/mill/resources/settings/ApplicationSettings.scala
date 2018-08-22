// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.resources.settings

import scalafx.beans.property.{BooleanProperty, StringProperty}

class ApplicationSettings {
  private val syntaxHighlightingEnabled = new BooleanProperty(null, "", true)
  private val lineNumbersVisible = new BooleanProperty(null, "", true)
  private val autoCompletePairedChars = new BooleanProperty(null, "", true)
  private val highlightCurrentLine = new BooleanProperty(null, "", true)
  private val productiveMode = new BooleanProperty(null, "", false)

  private val stickyProjectExplorer = new BooleanProperty(null, "", true)

  private val jdkPath = new StringProperty("")
  private val vmParameters = new StringProperty("")
  private val stickyEditorConsole = new BooleanProperty(null, "", true)

  def getSyntaxHighlightingEnabled: Boolean = syntaxHighlightingEnabled.get

  def syntaxHighlightingEnabledProperty: BooleanProperty = syntaxHighlightingEnabled

  def setSyntaxHighlightingEnabled(syntaxHighlightingEnabled: Boolean): Unit = {
    this.syntaxHighlightingEnabled.set(syntaxHighlightingEnabled)
  }

  def getLineNumbersVisible: Boolean = lineNumbersVisible.get

  def lineNumbersVisibleProperty: BooleanProperty = lineNumbersVisible

  def setLineNumbersVisible(lineNumbersVisible: Boolean): Unit = {
    this.lineNumbersVisible.set(lineNumbersVisible)
  }

  def getHighlightCurrentLine: Boolean = highlightCurrentLine.get

  def highlightCurrentLineProperty: BooleanProperty = highlightCurrentLine

  def setHighlightCurrentLine(highlightCurrentLine: Boolean): Unit = {
    this.highlightCurrentLine.set(highlightCurrentLine)
  }

  def getStickyProjectExplorer: Boolean = stickyProjectExplorer.get

  def setStickyProjectExplorer(stickyProjectExplorer: Boolean): Unit = {
    this.stickyProjectExplorer.set(stickyProjectExplorer)
  }

  def stickyProjectExplorerProperty: BooleanProperty = stickyProjectExplorer

  def getStickyEditorConsole: Boolean = stickyEditorConsole.get

  def stickyEditorConsoleProperty: BooleanProperty = stickyEditorConsole

  def setStickyEditorConsole(stickyEditorConsole: Boolean): Unit = {
    this.stickyEditorConsole.set(stickyEditorConsole)
  }

  def getJdkPath: String = jdkPath.get

  def jdkPathProperty: StringProperty = jdkPath

  def setJdkPath(jdkPath: String): Unit = {
    this.jdkPath.set(jdkPath)
  }

  def getVmParameters: String = vmParameters.get

  def vmParametersProperty: StringProperty = vmParameters

  def setVmParameters(vmParameters: String): Unit = {
    this.vmParameters.set(vmParameters)
  }

  def getAutoCompletePairedChars: Boolean = autoCompletePairedChars.get

  def autoCompletePairedCharsProperty: BooleanProperty = autoCompletePairedChars

  def setAutoCompletePairedChars(autoCompletePairedChars: Boolean): Unit = {
    this.autoCompletePairedChars.set(autoCompletePairedChars)
  }

  def getProductiveMode: Boolean = productiveMode.get

  def productiveModeProperty: BooleanProperty = productiveMode

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

