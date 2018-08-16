// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.views

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.control.SplitPane
import javafx.scene.layout.StackPane
import mill.controller.{AppController, FXStageInitializer}
import mill.resources.settings.ApplicationSettings
import mill.ui.EditorArea
import mill.ui.controls.SplitPaneDividerSlider
import mill.ui.views.explorer.ProjectExplorer

class ProjectView private() extends SplitPane with FXStageInitializer {
  private var editorPane: Node = _
  private var editorCenterStack: StackPane = _
  private var projectExplorerVisible = true
  private var slider: SplitPaneDividerSlider = _

  init()

  def init(): Unit = {
    editorPane = EditorArea.instance().init()
    editorCenterStack = new StackPane(editorPane)

    SplitPane.setResizableWithParent(ProjectExplorer.instance(), false)

    this.getItems.addAll(ProjectExplorer.instance(), editorCenterStack)
    this.setDividerPositions(0.2)

    slider = new SplitPaneDividerSlider(this, 0, SplitPaneDividerSlider.Direction.LEFT)

    ProjectExplorer.instance().widthProperty.addListener(new ChangeListener[Number] {
      override def changed(observableValue: ObservableValue[_ <: Number], t: Number, newValue: Number): Unit = {
        if (newValue.intValue > 0) slider.setCurrentDividerPosition(ProjectView.this.getDividerPositions()(0))
      }
    })

    AppController.instance().addFXStageInitializer(this)
  }

  def showView(content: Node): Unit = {
    editorCenterStack.getChildren.clear()

    if (content != null) editorCenterStack.getChildren.add(content)

    editorCenterStack.getChildren.add(editorPane)
  }

  def isProjectExplorerVisible: Boolean = projectExplorerVisible

  def setProjectExplorerVisible(visible: Boolean): Unit = {
    projectExplorerVisible = visible

    if (visible) slider.recomputeSize()
    else slider.setEventHandler((_: ActionEvent) => {
    })
    slider.setAimContentVisible(visible)
  }

  def slideProjectExplorer(mouseX: Double): Unit = {
    if (!ApplicationSettings.instance().getStickyProjectExplorer) {
      val sceneWidth = ProjectExplorer.instance().getScene.getWidth
      val sliderWidth = sceneWidth * slider.getLastDividerPosition
      val MARGIN = 20.0

      projectExplorerVisible = mouseX <= (sliderWidth + MARGIN)

      if (projectExplorerVisible) slider.recomputeSize()
      slider.setAimContentVisible(projectExplorerVisible)
    }
  }


  def getEditorPane: Node = editorPane

  override def fxInitialize: Boolean = true
}

object ProjectView {
  private var _instance: ProjectView = _

  def instance(): ProjectView = {
    if (_instance == null) _instance = new ProjectView()

    _instance
  }
}
