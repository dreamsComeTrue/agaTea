// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import java.io.File

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.{FXCollections, ListChangeListener}
import javafx.event.ActionEvent
import javafx.geometry.Orientation
import javafx.scene.control.{Label, SplitPane, Tab, TabPane}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{BorderPane, StackPane}
import mill.controller.{AppController, FXStageInitializer, GlobalState}
import mill.resources.Resource
import mill.resources.settings.ApplicationSettings
import mill.ui.controls.SplitPaneDividerSlider
import mill.ui.editor.{EditorBuffer, EditorWindow, EditorWindowContainer}
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.tuple.Pair

import scala.collection.JavaConverters._
import scala.io.Source

class EditorArea private() extends FXStageInitializer {
  private val tabPane = createTabPane()
  private var centerStack: StackPane = createCenterStack()
  private val editorConsole: EditorConsole = new EditorConsole
  private var consoleSplitPane: SplitPane = createConsoleSplitPane()
  private var consoleSlider: SplitPaneDividerSlider = createConsoleSlider()
  private var consoleWindowVisible = true

  private val windowContainer = new EditorWindowContainer
  private val editorWindows = FXCollections.observableArrayList[EditorWindow]
  private var activeEditorWindow: SimpleObjectProperty[EditorWindow] = _

  override def fxInitialize: Boolean = true

  def createCenterStack(): StackPane = {
    val centerContent = new BorderPane
    centerContent.setCenter(windowContainer)
    centerContent.setMinHeight(0.0)

    centerStack = new StackPane(createLogo(), centerContent)
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

  def closeResourceInEditor(path: String): Unit = {
    for (window <- editorWindows.asScala) {
      for (buffer <- window.getBuffers.asScala) {
        if (buffer.getPath == path) {
          window.removeBuffer(buffer)
          return
        }
      }
    }
  }

  def openResourceInEditor(title: String, path: String, resource: Resource): Unit = { //	Try to find already opened file
    for (window <- editorWindows.asScala) {
      for (buffer <- window.getBuffers.asScala) {
        if (buffer.getPath == path) return
      }
    }

    if (activeEditorWindow.getValue == null) {
      val window = new EditorWindow
      addEditorWindow(window)
      setActiveEditorWindow(window)
    }

    GlobalState.instance().addOpenedFile(path)

    val buffer = activeEditorWindow.get.addBuffer(title)
    buffer.openFile(FilenameUtils.normalize(path), resource)
  }

  private def addEditorWindow(window: EditorWindow): Unit = {
    editorWindows.add(window)
    activeEditorWindow.set(window)
    windowContainer.addWindow(window)

    //	Attach listener for window removal, when no buffer is active
    val buffers = window.getBuffers

    buffers.addListener(new ListChangeListener[EditorBuffer] {
      override def onChanged(c: ListChangeListener.Change[_ <: EditorBuffer]): Unit = {
        if (buffers.size == 0) removeEditorWindow(window)
      }
    })
  }

  def removeEditorWindow(window: EditorWindow): Unit = {
    windowContainer.removeWindow(window)
    editorWindows.remove(window)

    if (editorWindows.size > 0) activeEditorWindow.set(editorWindows.get(editorWindows.size - 1))
    else activeEditorWindow.set(null)
  }

  def splitToRight(buffer: EditorBuffer): Unit = {
    if (buffer.getWindow.getBuffers.size > 1) {
      val windows = splitFunction(buffer)

      windowContainer.splitToRight(windows.getLeft, windows.getRight)
      buffer.requestFocus()
    }
  }

  def splitToLeft(buffer: EditorBuffer): Unit = {
    if (buffer.getWindow.getBuffers.size > 1) {
      val windows = splitFunction(buffer)

      windowContainer.splitToLeft(windows.getLeft, windows.getRight)
      buffer.requestFocus()
    }
  }

  def splitToTop(buffer: EditorBuffer): Unit = {
    if (buffer.getWindow.getBuffers.size > 1) {
      val windows = splitFunction(buffer)

      windowContainer.splitToTop(windows.getLeft, windows.getRight)
      buffer.requestFocus()
    }
  }

  def splitToBottom(buffer: EditorBuffer): Unit = {
    if (buffer.getWindow.getBuffers.size > 1) {
      val windows = splitFunction(buffer)

      windowContainer.splitToBottom(windows.getLeft, windows.getRight)
      buffer.requestFocus()
    }
  }

  private def splitFunction(buffer: EditorBuffer) = { //	Store old pane
    val oldWindow = buffer.getWindow

    //	Create new window with selected buffer
    val window = new EditorWindow

    editorWindows.add(window)
    activeEditorWindow.get.removeBuffer(buffer)
    activeEditorWindow.set(window)
    activeEditorWindow.get.addBuffer(buffer)

    GlobalState.instance().addOpenedFile(buffer.getPath)

    //	Attach listener for window removal, when no buffer is active
    val buffers = window.getBuffers
    buffers.addListener(new ListChangeListener[EditorBuffer] {
      override def onChanged(c: ListChangeListener.Change[_ <: EditorBuffer]): Unit = {
        if (buffers.size == 0) removeEditorWindow(window)
      }
    })

    Pair.of(window, oldWindow)
  }

  def setActiveEditorWindow(window: EditorWindow): Unit = {
    //	Sets active buffer to NULL on PREVIOUS active editor window
    if (activeEditorWindow.get != window) {
      if (activeEditorWindow.get != null) activeEditorWindow.get.setActiveBuffer(null)

      activeEditorWindow.set(window)
    }
  }

  def getActiveEditorWindow: EditorWindow = activeEditorWindow.get

  def getActiveEditorBuffer: EditorBuffer = if (activeEditorWindow.get == null) null
  else activeEditorWindow.get.getActiveBuffer

  def actionMoveToPreviousBuffer(): Unit = {
    val buffers = getActiveEditorWindow.getBuffers
    var nextIndex = -1

    for (i <- 0 until buffers.size) {
      if (nextIndex == -1 && buffers.get(i) != getActiveEditorBuffer) {
        nextIndex = i - 1
      }
    }

    if (nextIndex < 0) nextIndex = buffers.size - 1
    getActiveEditorWindow.setActiveBuffer(buffers.get(nextIndex))
  }

  def actionMoveToNextBuffer(): Unit = {
    val buffers = getActiveEditorWindow.getBuffers
    var nextIndex = -1

    for (i <- 0 until buffers.size) {
      if (nextIndex == -1 && buffers.get(i) != getActiveEditorBuffer) {
        nextIndex = i + 1
      }
    }

    if (nextIndex >= buffers.size) nextIndex = 0
    getActiveEditorWindow.setActiveBuffer(buffers.get(nextIndex))
  }
}

object EditorArea {
  private var _instance: EditorArea = _

  def instance(): EditorArea = {
    if (_instance == null) _instance = new EditorArea()

    _instance
  }
}

