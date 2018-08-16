// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import java.io.File

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.ActionEvent
import javafx.geometry.Orientation
import javafx.scene.control.{Label, SplitPane, Tab, TabPane}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.StackPane
import mill.controller.{AppController, FXStageInitializer}
import mill.resources.Resource
import mill.resources.settings.ApplicationSettings
import mill.ui.controls.SplitPaneDividerSlider

import scala.io.Source

class EditorArea private() extends FXStageInitializer {
  def closeResourceInEditor(path: String): Unit = ???

  def openResourceInEditor(title: String, filePath: String, boundResource: Resource) = ???

  private val tabPane = createTabPane()
  private var centerStack: StackPane = createCenterStack()
  private val editorConsole: EditorConsole = new EditorConsole
  private var consoleSplitPane: SplitPane = createConsoleSplitPane()
  private var consoleSlider: SplitPaneDividerSlider = createConsoleSlider()
  private var consoleWindowVisible = true

  override def fxInitialize: Boolean = true

  def createCenterStack(): StackPane = {
    centerStack = new StackPane(createLogo(), tabPane)
    centerStack.getStyleClass.add("code-area")

    centerStack
  }

  def createConsoleSplitPane(): SplitPane = {
    editorConsole.init()

    consoleSplitPane = new SplitPane(centerStack, editorConsole)
    consoleSplitPane.setOrientation(Orientation.VERTICAL)
    consoleSplitPane.getStyleClass.add("split-pane")
    consoleSplitPane.setMinSize(0.0, 0.0)
    consoleSplitPane.setDividerPositions(0.8f)

    consoleSplitPane
  }

  def createConsoleSlider(): SplitPaneDividerSlider = {
    consoleSlider = new SplitPaneDividerSlider(consoleSplitPane, 0, SplitPaneDividerSlider.Direction.DOWN)
    consoleSlider.setLastDividerPosition(consoleSplitPane.getDividerPositions()(0))

    SplitPane.setResizableWithParent(editorConsole, false)

    editorConsole.heightProperty.addListener(new ChangeListener[Number] {
      override def changed(observableValue: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
        consoleSlider.setCurrentDividerPosition(consoleSplitPane.getDividerPositions()(0))
      }
    })

    consoleSlider
  }

  def init(): SplitPane = {
    consoleSplitPane
  }

  private def createLogo(): ImageView = {
    val label = new Label("mill")
    label.setId("center-logo")

    val image = new Image(getClass.getResourceAsStream("/logo.png"))

    val centerImage = new ImageView()
    centerImage.setImage(image)
    centerImage.setFitWidth(240)
    centerImage.setPreserveRatio(true)
    centerImage.setSmooth(true)

    centerImage
  }

  def addTab(header: String, text: String, path: String = ""): Unit = {
    val textEditor = new TextEditor(header, text, path)

    val tab = new Tab()
    tab.setText(header)
    tab.setContent(textEditor)

    tabPane.getTabs.add(tab)
    tabPane.getSelectionModel.select(tab)

    AppController.instance().assignCurrentTextEditor(Option(textEditor))
  }


  def addTab(file: File): Unit = {
    val header = file.getName
    val text = Source.fromFile(file).mkString

    addTab(header, text, file.getAbsolutePath)
  }

  def getCurrentTextEditor: TextEditor = {
    val selectedTab = tabPane.getSelectionModel.getSelectedItem

    selectedTab.getContent.asInstanceOf[TextEditor]
  }

  private def createTabPane(): TabPane = {
    val tabPane = new TabPane()

    tabPane.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[Tab] {
      override def changed(observableValue: ObservableValue[_ <: Tab], t: Tab, t1: Tab): Unit = {
        if (observableValue.getValue != null) {
          val textEditor = observableValue.getValue.getContent.asInstanceOf[TextEditor]
          AppController.instance().assignCurrentTextEditor(Option(textEditor))
        } else {
          AppController.instance().assignCurrentTextEditor(None)
        }
      }
    })

    tabPane
  }

  def isConsoleWindowVisible: Boolean = consoleWindowVisible

  def setConsoleWindowVisible(visible: Boolean): Unit = {
    consoleWindowVisible = visible

    if (visible) consoleSlider.recomputeSize()
    else consoleSlider.setEventHandler((_: ActionEvent) => {
    })

    consoleSlider.setAimContentVisible(visible)
  }

  def slideEditorConsole(mouseY: Double): Unit = {
    if (!ApplicationSettings.instance().getStickyEditorConsole) {
      val sceneHeight = editorConsole.output.getScene.getHeight
      val sliderHeight = sceneHeight * consoleSlider.getLastDividerPosition
      val MARGIN = 20.0

      consoleWindowVisible = mouseY >= sliderHeight - MARGIN

      if (consoleWindowVisible) consoleSlider.recomputeSize()

      consoleSlider.setAimContentVisible(consoleWindowVisible)
    }
  }
}

object EditorArea {
  private var _instance: EditorArea = _

  def instance(): EditorArea = {
    if (_instance == null) _instance = new EditorArea()

    _instance
  }
}

