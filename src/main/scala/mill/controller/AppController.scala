// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.controller

import java.io.File

import javafx.animation.{Animation, KeyFrame, Timeline}
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.util.Duration
import mill.ui.{EditorArea, MainContent, TextEditor}

import scala.collection.mutable.ListBuffer

class AppController private(val mainContent: MainContent) {
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
