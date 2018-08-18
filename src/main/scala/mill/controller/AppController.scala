// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.controller

import java.io.File

import javafx.animation.{Animation, KeyFrame, Timeline}
import javafx.application.Platform
import javafx.beans.property.DoubleProperty
import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.util.Duration
import mill.controller.FlowState.FlowState
import mill.resources.Resource
import mill.resources.settings.ApplicationSettings
import mill.ui.editor.{EditorBuffer, EditorWindow}
import mill.ui.views.ProjectView
import mill.ui.{EditorArea, FooterArea, MainContent, TextEditor}

import scala.collection.mutable.ListBuffer

class AppController private(val mainContent: MainContent) {
  private var scheduler: Timeline = _
  private var stageInitializers = new ListBuffer[FXStageInitializer]()

  init()

  private def init(): Unit = {
    scheduler = new Timeline(new KeyFrame(Duration.millis(1), (_: ActionEvent) => Platform.runLater(() => {
      for (initializer <- stageInitializers) {
        val result = initializer.fxInitialize
        if (result) stageInitializers -= initializer
      }

      if (stageInitializers.isEmpty) scheduler.stop()
    })))

    scheduler.setCycleCount(Animation.INDEFINITE)
    scheduler.play()
  }

  def addFXStageInitializer(stageInitializer: FXStageInitializer): Unit = {
    stageInitializers += stageInitializer
    scheduler.playFromStart()
  }

  def addTab(header: String, text: String, path: String = ""): Unit = {
    EditorArea.instance().addTab(header, text, path)
  }

  def addTab(file: File): Unit = {
    EditorArea.instance().addTab(file)
  }

  def getCurrentTextEditor: TextEditor = {
    EditorArea.instance().getCurrentTextEditor
  }

  def assignCurrentTextEditor(textEditorOpt: Option[TextEditor]): Unit = {
    mainContent.assignCurrentTextEditor(textEditorOpt)
  }

  def getConsoleWindowVisible: Boolean = EditorArea.instance().isConsoleWindowVisible

  def setConsoleWindowVisible(visible: Boolean): Unit = {
    EditorArea.instance().setConsoleWindowVisible(visible)
  }

  def setFlowState(state: FlowState): Unit = {
    StateManager.instance().setActualState(state)
  }

  def switchToLastState(): Unit = {
    StateManager.instance().switchToLastState()
  }

  def setFooterMessageText(text: String): Unit = {
    FooterArea.instance().setInfoText(text)
  }

  def bindStateManager(): Unit = {
    StateManager.instance().bindStates()
  }

  def getProjectExplorerVisible: Boolean = ProjectView.instance().isProjectExplorerVisible

  def setProjectExplorerVisible(visible: Boolean): Unit = {
    ProjectView.instance().setProjectExplorerVisible(visible)
  }

  def switchProductiveMode(showMaximized: Boolean): Unit = {
    ApplicationSettings.instance().setProductiveMode(showMaximized)

    setProjectExplorerVisible(!showMaximized)
    setConsoleWindowVisible(!showMaximized)

    mainContent.setHeaderAreaVisible(!showMaximized)
  }

  def isProductiveMode: Boolean = ApplicationSettings.instance().getProductiveMode

  def closeResourceInEditor(path: String): Unit = EditorArea.instance().closeResourceInEditor(path)

  def showContentBar(imageName: String, content: Pane, initialFocus: Node): Unit = mainContent.showContentBar(imageName, content, initialFocus)

  def setContentBarHeight(height: Int): Unit = mainContent.setContentBarHeight(height)

  def hideContentBar(): Unit = mainContent.hideContentBar()

  def showNotification(text: String): Unit = mainContent.showNotification(text)

  def openResourceInEditor(title: String, filePath: String, boundResource: Resource): Unit = EditorArea.instance().openResourceInEditor(title, filePath, boundResource)

  def focusEditor(filePath: String): Boolean = EditorArea.instance().focusEditorBuffer(filePath)

  def maximizeEditorBuffer(maximize: Boolean): Unit = {
    setProjectExplorerVisible(maximize)
    setConsoleWindowVisible(maximize)
  }

  def setActiveEditorWindow(editorWindow: EditorWindow): Unit = {
    EditorArea.instance().setActiveEditorWindow(editorWindow)
  }

  def getActiveEditorBuffer: EditorBuffer = EditorArea.instance().getActiveEditorBuffer

  def getCurrentFontSize: DoubleProperty = getActiveEditorBuffer.getTextEditor.getFontSizeProperty

}

object AppController {
  private var _instance: AppController = _

  def initialize(mainContent: MainContent): Unit = {
    if (_instance == null) _instance = new AppController(mainContent)
  }

  def instance(): AppController = {
    _instance
  }
}
