// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.controller

import java.io.File

import javafx.animation.{Animation, KeyFrame, Timeline}
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.util.Duration
import mill.controller.FlowState.FlowState
import mill.resources.Resource
import mill.resources.settings.ApplicationSettings
import mill.ui.views.ProjectView
import mill.ui.{EditorArea, FooterArea, MainContent, TextEditor}

import scala.collection.mutable.ListBuffer

class AppController private(val mainContent: MainContent) {
  def closeResourceInEditor(path: String): Unit = ???

  def showContentBar(imageName: String, content: Pane, initialFocus: Node): Unit = {
  }

  def setContentBarHeight(i: Int) = ???

  def hideContentBar(): Unit = ???

  def showNotification(SELECT_ONE_OF_RESOURCES: String) = ???

  def openResourceInEditor(fileName: String, filePath: String, resource: Resource) = ???

  def focusEditor(filePath: String): Boolean = ???

  private var scheduler: Timeline = _
  private var stageInitializers = new ListBuffer[FXStageInitializer]()

  init()

  def init(): Unit = {
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
