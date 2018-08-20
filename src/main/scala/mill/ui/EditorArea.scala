// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import java.io.File

import com.sun.javafx.scene.control.skin.SplitPaneSkin
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.ActionEvent
import javafx.geometry.Orientation
import javafx.scene.control.{Label, SplitPane, Tab, TabPane}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.layout.{BorderPane, StackPane}
import mill.controller.{AppController, FXStageInitializer, GlobalState}
import mill.resources.{Resource, ResourceFactory}
import mill.resources.settings.ApplicationSettings
import mill.ui.controls.SplitPaneDividerSlider
import mill.ui.editor.{EditorBuffer, EditorWindow, EditorWindowContainer}
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.tuple.Pair
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.collections.ObservableBuffer.Change

import scala.io.Source

class EditorArea private() extends FXStageInitializer {
  private val windowContainer = new EditorWindowContainer
  private val editorWindows = new ObservableBuffer[EditorWindow]()
  private val activeEditorWindow = new ObjectProperty[EditorWindow]()

  private val tabPane = createTabPane()
  private var centerStack: StackPane = createCenterStack()
  private val editorConsole: EditorConsole = new EditorConsole
  private var consoleSplitPane: SplitPane = createConsoleSplitPane()
  private var consoleSlider: SplitPaneDividerSlider = createConsoleSlider()
  private var consoleWindowVisible = true

  def init(): SplitPane = {
    initActiveEditorWindow()

    consoleSplitPane
  }

  def initActiveEditorWindow(): Unit = {
    activeEditorWindow.onChange { (_, oldWindow: EditorWindow, newWindow: EditorWindow) => {
      if (oldWindow != null) {
        oldWindow.setActiveBuffer(null)
        oldWindow.setActive(false)
      }

      if (newWindow != null) newWindow.setActive(true)
      else FooterArea.instance().setPosLabel(0, 0)
    }
    }
  }

  override def fxInitialize: Boolean = {
    val skin = consoleSplitPane.getSkin.asInstanceOf[SplitPaneSkin]
    val divider = skin.getChildren.get(2)

    divider.setOnMouseClicked((event: MouseEvent) => {
      if (event.getButton == MouseButton.PRIMARY && event.getClickCount == 2) setConsoleWindowVisible(!isConsoleWindowVisible)
    })

    if (activeEditorWindow() != null) activeEditorWindow().moveFocusToActiveBuffer()

    true
  }

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

  def getEditorConsole = editorConsole

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
    for (i <- 0 until editorWindows.size()) {
      val window = editorWindows(i)

      for (i <- 0 until window.getBuffers.size()) {
        val buffer = window.getBuffers.get(i)

        if (buffer.getPath == path) {
          window.removeBuffer(buffer)
          return
        }
      }
    }
  }

  def openResourceInEditor(title: String, path: String, resource: Resource): Unit = { //	Try to find already opened file
    for (window <- editorWindows) {
      for (buffer <- window.getBuffers) {
        if (buffer.getPath == path) {
          return
        }
      }
    }

    if (activeEditorWindow() == null) {
      val window = new EditorWindow
      addEditorWindow(window)
      setActiveEditorWindow(window)
    }

    GlobalState.instance().addOpenedFile(path)

    val buffer = activeEditorWindow().addBuffer(title)
    buffer.openFile(FilenameUtils.normalize(path), resource)
  }

  private def addEditorWindow(window: EditorWindow): Unit = {
    editorWindows += window

    activeEditorWindow() = window
    windowContainer.addWindow(window)

    //	Attach listener for window removal, when no buffer is active
    val buffers = window.getBuffers

    buffers.onChange((_: ObservableBuffer[EditorBuffer], c: Seq[Change[EditorBuffer]]) => if (buffers.isEmpty) removeEditorWindow(window))
  }

  def removeEditorWindow(window: EditorWindow): Unit = {
    windowContainer.removeWindow(window)
    editorWindows.remove(window)

    if (editorWindows.nonEmpty) activeEditorWindow() = editorWindows.get(editorWindows.size - 1)
    else activeEditorWindow() = null
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
    activeEditorWindow().removeBuffer(buffer)
    activeEditorWindow() = window
    activeEditorWindow().addBuffer(buffer)

    GlobalState.instance().addOpenedFile(buffer.getPath)

    //	Attach listener for window removal, when no buffer is active
    val buffers = window.getBuffers
    buffers.onChange((_: ObservableBuffer[EditorBuffer], c: Seq[Change[EditorBuffer]]) => if (buffers.isEmpty) removeEditorWindow(window))

    Pair.of(window, oldWindow)
  }

  def setActiveEditorWindow(window: EditorWindow): Unit = {
    //	Sets active buffer to NULL on PREVIOUS active editor window
    if (activeEditorWindow() != window) {
      if (activeEditorWindow() != null) activeEditorWindow().setActiveBuffer(null)

      activeEditorWindow.set(window)
    }
  }

  def getActiveEditorWindow: EditorWindow = activeEditorWindow()

  def getActiveEditorBuffer: EditorBuffer = {
    if (activeEditorWindow() == null) null
    else activeEditorWindow().getActiveBuffer
  }

  def actionMoveToPreviousBuffer(): Unit = {
    val buffers = getActiveEditorWindow.getBuffers
    var nextIndex = -1

    for (i <- buffers.indices) {
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

    for (i <- buffers.indices) {
      if (nextIndex == -1 && buffers.get(i) != getActiveEditorBuffer) {
        nextIndex = i + 1
      }
    }

    if (nextIndex >= buffers.size) nextIndex = 0
    getActiveEditorWindow.setActiveBuffer(buffers.get(nextIndex))
  }

  def actionCloseDocument(): Unit = {
    if (getActiveEditorBuffer != null) closeResourceInEditor(getActiveEditorBuffer.getPath)
  }

  def actionSwitchToPreviousFile(): Unit = {
//    val openedFilesCount = openedFilesList.getItems.size
//    val recentFilesCount = recentFilesList.getItems.size
//
//    if ((openedFilesCount > 0 || recentFilesCount > 0) && !windowSwitcher.isVisible) {
//      currFileSwitcherIndex = -1
//      windowSwitcher.setVisible(true)
//    }
//    if (windowSwitcher.isVisible) {
//      currFileSwitcherIndex -= 1
//      if (currFileSwitcherIndex < 0) currFileSwitcherIndex = openedFilesCount + recentFilesCount - 1
//      if (currFileSwitcherIndex < openedFilesCount) {
//        openedFilesList.requestFocus()
//        openedFilesList.getSelectionModel.select(currFileSwitcherIndex)
//        openedFilesList.getFocusModel.focus(currFileSwitcherIndex)
//      }
//      else {
//        recentFilesList.requestFocus()
//        recentFilesList.getSelectionModel.select(currFileSwitcherIndex - openedFilesCount)
//        recentFilesList.getFocusModel.focus(currFileSwitcherIndex - openedFilesCount)
//      }
//    }
  }

  def actionSwitchToNextFile(): Unit = {
//    val openedFilesCount = openedFilesList.getItems.size
//    val recentFilesCount = recentFilesList.getItems.size
//    if ((openedFilesCount > 0 || recentFilesCount > 0) && !windowSwitcher.isVisible) {
//      currFileSwitcherIndex = -1
//      windowSwitcher.setVisible(true)
//    }
//    if (windowSwitcher.isVisible) {
//      currFileSwitcherIndex += 1
//      if (currFileSwitcherIndex >= openedFilesCount + recentFilesCount) currFileSwitcherIndex = 0
//      if (currFileSwitcherIndex < openedFilesCount) {
//        openedFilesList.requestFocus()
//        openedFilesList.getSelectionModel.select(currFileSwitcherIndex)
//        openedFilesList.getFocusModel.focus(currFileSwitcherIndex)
//      }
//      else {
//        recentFilesList.requestFocus()
//        recentFilesList.getSelectionModel.select(currFileSwitcherIndex - openedFilesCount)
//        recentFilesList.getFocusModel.focus(currFileSwitcherIndex - openedFilesCount)
//      }
//    }
  }

  def actionHideWindowSwitcher(): Unit = {
//    if (windowSwitcher.isVisible) {
//      windowSwitcher.setVisible(false)
//
//      val openedFilesCount = openedFilesList.getItems.size
//      var selectedItem = ""
//
//      if (currFileSwitcherIndex < openedFilesCount) selectedItem = openedFilesList.getSelectionModel.getSelectedItem
//      else selectedItem = recentFilesList.getSelectionModel.getSelectedItem
//
//      ResourceFactory.handleFileOpen(selectedItem)
//
//      openedFilesList.getItems.remove(selectedItem)
//      openedFilesList.getItems.add(0, selectedItem)
//    }
  }


  /**
    * Focuses on buffer with given title
    *
    * @param filePath buffer to move focus to
    */
  def focusEditorBuffer(filePath: String): Boolean = {
    val normalFilePath = FilenameUtils.normalize(filePath)

    for (window <- editorWindows) {
      for (buffer <- window.getBuffers) {
        if (buffer.getPath == normalFilePath) {
          setActiveEditorWindow(window)
          window.setActiveBuffer(buffer)

          AppController.instance().addFXStageInitializer(this)
          return true
        }
      }
    }

    false
  }

  /**
    * Moves focus to active editor buffer
    */
  def requestFocus(): Unit = {
    if (getActiveEditorBuffer != null) getActiveEditorBuffer.requestFocus()
  }
}

object EditorArea {
  private var _instance: EditorArea = _

  def instance(): EditorArea = {
    if (_instance == null) _instance = new EditorArea()

    _instance
  }
}

