// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.views

import javafx.scene.Node
import javafx.scene.control.SplitPane
import javafx.scene.layout.StackPane
import mill.controller.{AppController, FXStageInitializer}
import mill.ui.EditorArea

class ProjectView private() extends SplitPane with FXStageInitializer {
  private var editorPane: Node = _
  private var editorCenterStack: StackPane = _

  init()

  def init(): Unit = {
    editorPane = EditorArea.initialize().init()
    editorCenterStack = new StackPane(editorPane)

    this.getItems.addAll(editorCenterStack)
    this.setDividerPositions(0.2)

    AppController.instance().addFXStageInitializer(this)
  }

  def getEditorPane: Node = editorPane
  override def fxInitialize: Boolean = true
}

object ProjectView {
  private var _instance: ProjectView = _

  def initialize(): ProjectView = {
    if (_instance == null) _instance = new ProjectView()

    _instance
  }

  def instance(): ProjectView = {
    _instance
  }
}
