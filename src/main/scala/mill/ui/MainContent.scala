// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import java.io.File
import java.lang

import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.control.ListView
import javafx.scene.input._
import javafx.scene.layout._
import javafx.scene.{Node, Scene}
import javafx.stage.{FileChooser, Stage}
import mill.controller.{AppController, ApplicationState, FlowState, StateManager}
import mill.model.ActionShortcut
import mill.resources.ResourceFactory
import mill.resources.settings.ApplicationSettings
import mill.ui.controls.SlideNotificationBar
import mill.ui.views._
import mill.ui.views.explorer.ProjectExplorer
import mill.{EditorMode, Resources, ViewTransition}

class MainContent private(stage: Stage) extends BorderPane {
  private val stylesURL = getClass.getResource("/app_styles.css").toExternalForm
  private val dialogURL = getClass.getResource("/dialog_styles.css").toExternalForm
  private val splitPaneURL = getClass.getResource("/split_pane.css").toExternalForm
  private val scrollbarsURL = getClass.getResource("/scrollbars.css").toExternalForm
  private val tabPaneURL = getClass.getResource("/tab_pane.css").toExternalForm
  private val codeAreaURL = getClass.getResource("/code_area.css").toExternalForm
  private val searchBoxURL = getClass.getResource("/search_box.css").toExternalForm

  private val styles = List(stylesURL, dialogURL, splitPaneURL, scrollbarsURL, tabPaneURL, codeAreaURL, searchBoxURL)

  private val windowSwitcher: WindowSwitcher = new WindowSwitcher
  private val windowsList: ListView[String] = new ListView[String]
  var scene: Scene = _
  var topPane: GridPane = _
  var bottomPane: FooterArea = _
  var centerPane: StackPane = _
  var filePath: String = new File(".").getCanonicalPath

  private var transition: ViewTransition = _
  private var viewsTransition: ViewTransition = _

  private var snb: SlideNotificationBar = _

  init()

  private def init(): Unit = {
    scene = new Scene(this, 1200, 700)
    scene.getStylesheets.addAll(styles: _*)
    scene.addEventFilter(KeyEvent.KEY_PRESSED,
      (event: KeyEvent) => {
        if (event.isShiftDown) {
          event.getCode match {
            case KeyCode.SEMICOLON =>
              EditorMode.mode.set(EditorMode.COMMAND_MODE)
            case _ => Unit
          }
        } else {
          event.getCode match {
            case KeyCode.ESCAPE =>
              EditorMode.mode.set(EditorMode.NORMAL_MODE)
            case _ => Unit
          }
        }
      })

    topPane = HeaderArea.instance()
    bottomPane = FooterArea.instance()
    centerPane = createCenterPane()

    snb = new SlideNotificationBar(centerPane, null)

    FileSelectView.instance().setFileCanceledEvent((_: FileSelectView.FileSelectEvent) => AppController.instance().switchToLastState())

    setTop(topPane)
    setBottom(bottomPane)
    setCenter(snb)

    assignCurrentTextEditor(None)

    scene.setOnDragOver((event: DragEvent) => {
      val db = event.getDragboard
      if (db.hasFiles) event.acceptTransferModes(TransferMode.COPY)
      else event.consume()
    })

    // Dropping over surface
    scene.setOnDragDropped((event: DragEvent) => {
      val db = event.getDragboard
      var success = false

      if (db.hasFiles) {
        success = true

        for (i <- 0 until db.getFiles.size()) {
          val filePath = db.getFiles.get(i).getAbsolutePath
          ResourceFactory.handleFileOpen(filePath)
        }
      }

      event.setDropCompleted(success)
      event.consume()
    })

    scene.addEventFilter(MouseEvent.MOUSE_MOVED, (event: MouseEvent) => {
      if (!ApplicationSettings.instance().getStickyProjectExplorer) ProjectView.instance().slideProjectExplorer(event.getSceneX)
      if (!ApplicationSettings.instance().getStickyEditorConsole) EditorArea.instance().slideEditorConsole(event.getSceneY)
    })

    ApplicationSettings.instance().productiveModeProperty.addListener(new ChangeListener[lang.Boolean] {
      override def changed(observableValue: ObservableValue[_ <: lang.Boolean], t: lang.Boolean, newValue: lang.Boolean): Unit = {
        AppController.instance().switchProductiveMode(newValue)
      }
    })

    this.setOnKeyPressed((event: KeyEvent) => {
      val flowState = StateManager.instance().getLastFlowState

      if (ActionShortcut.checkKeyCombination(ActionShortcut.ActionType.CLOSE_DOCUMENT, event)) EditorArea.instance().actionCloseDocument()
      else if (ActionShortcut.checkKeyCombination(ActionShortcut.ActionType.SELECT_PREVIOUS_BUFFER, event)) EditorArea.instance().actionMoveToPreviousBuffer()
      else if (ActionShortcut.checkKeyCombination(ActionShortcut.ActionType.SELECT_NEXT_BUFFER, event)) EditorArea.instance().actionMoveToNextBuffer()
      else if (ActionShortcut.checkKeyCombination(ActionShortcut.ActionType.SELECT_NEXT_BUFFER, event)) EditorArea.instance().actionMoveToNextBuffer()
      else if (ActionShortcut.checkKeyCombination(ActionShortcut.ActionType.SWITCH_TO_PREVIOUS_DOCUMENT, event)) EditorArea.instance().actionSwitchToPreviousFile()
      else if (ActionShortcut.checkKeyCombination(ActionShortcut.ActionType.SWITCH_TO_NEXT_DOCUMENT, event)) EditorArea.instance().actionSwitchToNextFile()
      else if (ActionShortcut.checkKeyCombination(ActionShortcut.ActionType.SELECT_PREVIOUS_TOOL_WINDOW, event)) windowSwitcher.actionSwitchToPreviousToolWindow()
      else if (ActionShortcut.checkKeyCombination(ActionShortcut.ActionType.SELECT_NEXT_TOOL_WINDOW, event)) windowSwitcher.actionSwitchToNextToolWindow()
      else if (ActionShortcut.checkKeyCombination(ActionShortcut.ActionType.QUICK_ACCESS, event)) HeaderArea.instance().activateQuickAccess()
      else if (ActionShortcut.checkKeyCombination(ActionShortcut.ActionType.NEW_FILE, event)) AppController.instance().setFlowState(FlowState.NEW_RESOURCE)
      else if (ActionShortcut.checkKeyCombination(ActionShortcut.ActionType.OPEN_FILE, event)) AppController.instance().setFlowState(FlowState.OPEN_RESOURCE)
      else if (ActionShortcut.checkKeyCombination(ActionShortcut.ActionType.OPEN_SETTINGS, event)) AppController.instance().setFlowState(FlowState.SETTINGS)
      else if (ActionShortcut.checkKeyCombination(ActionShortcut.ActionType.PRODUCTIVE_MODE, event)) AppController.instance().switchProductiveMode(!AppController.instance().isProductiveMode)
      else event.getCode match {
        case KeyCode.ESCAPE =>
          if (flowState != null) flowState match {
            case FlowState.NEW_RESOURCE => NewResourceView.instance().keyPressed(event)
            case FlowState.OPEN_RESOURCE => OpenResourceView.instance().keyPressed(event)
            case FlowState.SETTINGS => SettingsView.instance().keyPressed(event)
          }

        case _ =>
      }
    })

    this.setOnKeyReleased((event: KeyEvent) => {
      if (ActionShortcut.actions(ActionShortcut.ActionType.SWITCH_TO_NEXT_DOCUMENT).getBinding.head == event.getCode) EditorArea.instance().actionHideWindowSwitcher()
      if (ActionShortcut.actions(ActionShortcut.ActionType.SELECT_NEXT_TOOL_WINDOW).getBinding.head == event.getCode) hideToolWindowSwitcher()
    })

    AppController.instance().bindStateManager(this)
  }

  private def hideToolWindowSwitcher(): Unit = {
    if (windowSwitcher.isVisible) {
      windowSwitcher.setVisible(false)

      val selectedWindow = windowsList.getSelectionModel.getSelectedItem

      selectedWindow match {
        case Resources.PROJECT_EXPLORER_WINDOW_NAME =>
          ProjectExplorer.instance().requestFocus()
        case Resources.EDITOR_WINDOW_NAME =>
          EditorArea.instance().requestFocus()
        case Resources.CONSOLE_WINDOW_NAME =>
          EditorArea.instance().getEditorConsole.requestFocus()
        case _ =>
      }
    }
  }

  private def createCenterPane(): StackPane = {
    val centerPane = new StackPane

    ProjectView.instance().setPrefWidth(Double.MaxValue)
    ProjectView.instance().setPrefHeight(Double.MaxValue)

    transition = new ViewTransition(ProjectView.instance(), StructureView.instance()) {
      override def onFirstViewStart(): Unit = {
      }

      override def onFirstViewCompleted(): Unit = {
        HeaderArea.instance().switchView(projectView = true)
      }

      override def onFirstTransitionFinished(): Unit = {
      }

      override def onSecondViewCompleted(): Unit = {
      }
    }

    viewsTransition = new ViewTransition(NewResourceView.instance(), ProjectView.instance()) {
      override def onFirstViewStart(): Unit = {
        NewResourceView.instance().resetState()
      }

      override def onFirstViewCompleted(): Unit = {
        NewResourceView.instance().setVisible(true)
      }

      override def onFirstTransitionFinished(): Unit = {
      }

      override def onSecondViewCompleted(): Unit = {
      }
    }

    StructureView.initialize(centerPane, scene)
    StructureView.instance().setVisible(false)

    centerPane.getChildren.addAll(StructureView.instance(), ProjectView.instance(), windowSwitcher)
    centerPane
  }

  def setFocus(node: Node): Unit = {
    Platform.runLater(() => {
      if (!node.isFocused) {
        node.requestFocus()
        setFocus(node)
      }
    })
  }

  def assignCurrentTextEditor(textEditorOpt: Option[TextEditor]): Unit = {
    if (textEditorOpt.isDefined) {
      val textEditor = textEditorOpt.get
      val codeArea = textEditor.codeAreaVirtual.getContent

      EditorMode.mode.addListener(new ChangeListener[Number] {
        override def changed(observableValue: ObservableValue[_ <: Number], t: Number, t1: Number): Unit = {
          if (t1.intValue() == EditorMode.NORMAL_MODE) codeArea.requestFocus()
        }
      })

      setFocus(codeArea)

      def changeFunc(): Unit = {
        FooterArea.instance().setInfoText(if (textEditor.path != "") textEditor.path else textEditor.tabName)
        FooterArea.instance().setPosLabel(codeArea.getCaretColumn + 1, codeArea.getCaretSelectionBind.getParagraphIndex + 1)
      }

      changeFunc()

      codeArea.caretPositionProperty().addListener(new ChangeListener[Integer] {
        override def changed(observableValue: ObservableValue[_ <: Integer], t: Integer, t1: Integer): Unit = {
          changeFunc()
        }
      })
    } else {
      FooterArea.instance().setInfoText("")
      FooterArea.instance().setPosLabel(0, 0)
    }
  }

  def getFileDialog(title: String): File = {
    val fileChooser = new FileChooser

    fileChooser.setInitialDirectory(new File(filePath))
    fileChooser.setTitle(title)
    fileChooser.showOpenDialog(stage)
  }

  def runStatesTransition(state: ApplicationState, fromNode: Node, toNode: Node, runTransition: Boolean): Unit = {
    viewsTransition.setFromView(fromNode)
    viewsTransition.setToView(toNode)

    if (state.getOnFirstViewStart != null) viewsTransition.onFirstViewStartFunc = state.getOnFirstViewStart
    if (state.getOnFirstViewCompleted != null) viewsTransition.onFirstViewCompletedFunc = state.getOnFirstViewCompleted
    if (state.getOnFirstTransitionFinished != null) viewsTransition.onFirstTransitionFinishedFunc = state.getOnFirstTransitionFinished
    if (state.getOnSecondViewCompleted != null) viewsTransition.onSecondViewCompletedFunc = state.getOnSecondViewCompleted

    viewsTransition.runTransition(runTransition)
  }

  def setProjectViewMode(fromNode: Node, toNode: Node, isProjectView: Boolean): Unit = {
    transition.setFromView(fromNode)
    transition.setToView(toNode)
    transition.runTransition(isProjectView)
  }

  def setHeaderAreaVisible(visible: Boolean): Unit = {
    if (visible) this.setTop(HeaderArea.instance())
    else this.setTop(null)
  }

  /**
    * Shows popup bar at the top of workspace
    *
    * @param text text to be shown
    */
  def showNotification(text: String): Unit = {
    snb.setBarHeight(SlideNotificationBar.DEFAULT_HEIGHT)
    snb.showIcon(Resources.Images.IMAGE_WARNING)
    snb.setText(text, fontSize = 15)
    snb.show(easeTime = 300, showDuration = 2000, closeImmediately = true)
  }

  /**
    * Shows popup bar at the top of workspace with custom content
    *
    * @param content      content to be shown
    * @param initialFocus first node to set focus after showing
    */
  def showContentBar(iconName: String, content: Pane, initialFocus: Node): Unit = {
    snb.setOwnContent(content)
    snb.showIcon(iconName)
    snb.enableOnClickDisposal(enable = false)
    snb.setInitialFocusNode(initialFocus)
    snb.show(easeTime = 300, showDuration = 2000, closeImmediately = false)
  }

  /**
    * Hides popup bar at the top of workspace with custom content
    */
  def hideContentBar(): Unit = {
    snb.hide()
  }

  def setContentBarHeight(height: Double): Unit = {
    snb.setBarHeight(height)
  }

  //topPane.setMinHeight(1)
  //topPane.setMaxHeight(1)
}


object MainContent {
  private var _instance: MainContent = _

  def initialize(stage: Stage): MainContent = {
    if (_instance == null) _instance = new MainContent(stage)

    _instance
  }

  def instance(): MainContent = {
    _instance
  }
}