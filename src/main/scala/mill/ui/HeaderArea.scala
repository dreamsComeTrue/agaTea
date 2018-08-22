// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import java.io.{BufferedWriter, File, FileWriter}

import javafx.beans.binding.Bindings
import javafx.event.ActionEvent
import javafx.geometry.{HPos, Insets, Pos}
import javafx.scene.control.{Button, ToggleButton, Tooltip}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.KeyEvent
import javafx.scene.layout._
import mill.controller.{AppController, FlowState}
import mill.ui.controls.{SearchBox, SlideButton}
import mill.{FxDialogs, Resources, Utilities}

class HeaderArea private() extends GridPane {
  private final val buttonSize = 25
  private final val buttonPadding = 6

  private var viewButton: ToggleButton = _

  private val quickAccessBox = createQuickAccessBox()
  private val generalToolbar = createGeneralToolbar()
  private val buildToolbar = createBuildToolbar()
  private val miscToolbar = createMiscToolbar()

  init()

  private def init(): Unit = {
    this.setMinHeight(45)

    layoutToolbars()
    assignColumnConstraints()
  }

  private def createGeneralToolbar(): HBox = {
    val newEditorButton = createNewEditorButton
    val openButton = createOpenResourceButton
    val saveButton = createSaveButton

    HBox.setMargin(openButton, new Insets(0, 0, 0, 3))
    HBox.setMargin(saveButton, new Insets(0, 0, 0, 3))

    val bar = new HBox(newEditorButton, openButton, saveButton)
    bar.setAlignment(Pos.CENTER)
    bar.setPadding(new Insets(0, 0, 0, 15))

    bar
  }

  private def createBuildToolbar(): HBox = {
    val compileButton = createCompileButton
    val debugButton = createDebugButton
    HBox.setMargin(debugButton, new Insets(0, 0, 0, 3))

    val bar = new HBox(compileButton, debugButton)
    bar.setAlignment(Pos.CENTER)

    bar
  }

  private def createMiscToolbar(): HBox = {
    val image3DView = new Image(Utilities.getResource(Resources.Images.IMAGE_STRUCTURE_VIEW))
    val imageProject = new Image(Utilities.getResource(Resources.Images.IMAGE_PROJECT_VIEW))
    val imageViewView = new ImageView(imageProject)
    imageViewView.setFitHeight(20)
    imageViewView.setFitWidth(20)

    val projectTooltip = new Tooltip(Resources.SET_PROJECT_MODE)
    val structureTooltip = new Tooltip(Resources.SET_STRUCTURE_MODE)

    viewButton = new ToggleButton("", imageViewView)
    viewButton.setMaxSize(buttonSize, buttonSize)
    viewButton.setPrefSize(buttonSize, buttonSize)
    viewButton.setMinSize(buttonSize, buttonSize)
    viewButton.setFocusTraversable(false)
    viewButton.setTooltip(structureTooltip)

    imageViewView.fitWidthProperty.bind(Bindings.subtract(viewButton.widthProperty, buttonPadding))
    imageViewView.fitHeightProperty.bind(Bindings.subtract(viewButton.heightProperty, buttonPadding))
    viewButton.setOnAction((_: ActionEvent) => {
      if (viewButton.isSelected) {
        AppController.instance().setFlowState(FlowState.APPLICATION_STRUCTURE)

        imageViewView.setImage(image3DView)
        viewButton.setTooltip(projectTooltip)
      }
      else {
        AppController.instance().setFlowState(FlowState.APPLICATION_PROJECT)

        imageViewView.setImage(imageProject)
        viewButton.setTooltip(structureTooltip)
      }
    })
    viewButton.selectedProperty.addListener((_, _, _) => {
      if (viewButton.isSelected) imageViewView.setImage(image3DView)
      else imageViewView.setImage(imageProject)
    })

    val settingsButton = Utilities.createButton(Resources.Images.IMAGE_SETTINGS, buttonSize, buttonPadding)
    settingsButton.setFocusTraversable(false)
    settingsButton.setOnAction(_ => AppController.instance().setFlowState(FlowState.SETTINGS)
    )

    HBox.setMargin(settingsButton, new Insets(0, 0, 0, 10))

    val imageLayout = new Image(Utilities.getResource(Resources.Images.IMAGE_LAYOUT))
    val imageLayoutView = new ImageView(imageLayout)
    imageLayoutView.setFitHeight(10)
    imageLayoutView.setFitWidth(10)

    val layoutButton = new SlideButton("", imageLayoutView, buttonSize)
    layoutButton.setHorizontalOrientation(false)
    layoutButton.setFocusTraversable(false)
    layoutButton.setPrimaryButtonAction(true)
    layoutButton.setAlignPos(Pos.BOTTOM_CENTER)

    val projectExplorerButton = Utilities.createToggleButton(Resources.Images.IMAGE_PROJECT_VIEW, buttonSize, buttonPadding)
    projectExplorerButton.setOnAction((_: ActionEvent) => AppController.instance().setProjectExplorerVisible(!AppController.instance().getProjectExplorerVisible)
    )

    val outputConsoleButton = Utilities.createToggleButton(Resources.Images.IMAGE_WINDOW, buttonSize, buttonPadding)
    outputConsoleButton.setOnAction((_: ActionEvent) => AppController.instance().setConsoleWindowVisible(!AppController.instance().getConsoleWindowVisible))

    AnchorPane.setLeftAnchor(projectExplorerButton, 5.0)
    AnchorPane.setTopAnchor(projectExplorerButton, 5.0)
    AnchorPane.setLeftAnchor(outputConsoleButton, 35.0)
    AnchorPane.setTopAnchor(outputConsoleButton, 5.0)

    val buttonsPane = new AnchorPane(projectExplorerButton, outputConsoleButton)
    buttonsPane.setStyle("-fx-background-color: rgb(80,80,80);" + "-fx-border-color: rgb(50, 50, 50)")
    buttonsPane.setPrefWidth(100)
    buttonsPane.setPrefHeight(200)

    layoutButton.setContent(buttonsPane)
    imageLayoutView.fitWidthProperty.bind(Bindings.subtract(layoutButton.widthProperty, 8))
    imageLayoutView.fitHeightProperty.bind(Bindings.subtract(layoutButton.heightProperty, 8))

    HBox.setMargin(quickAccessBox, new Insets(0, 10, 0, 0))
    HBox.setMargin(layoutButton, new Insets(0, 0, 0, 3))
    HBox.setMargin(settingsButton, new Insets(0, 0, 0, 3))

    val bar = new HBox(quickAccessBox, viewButton, layoutButton, settingsButton)
    bar.setAlignment(Pos.CENTER_RIGHT)
    bar.setPadding(new Insets(0, 15, 0, 0))

    bar
  }


  private def createQuickAccessBox(): SearchBox = {
    val box = new SearchBox
    box.setFocusTraversable(false)
    box.getTextBox.setPromptText(Resources.QUICK_ACCESS)
    box.getTextBox.setOnKeyPressed((_: KeyEvent) => {
    })

    box
  }

  private def createDebugButton: Button = {
    val debugButton = Utilities.createButton(Resources.Images.IMAGE_DEBUG, buttonSize, buttonPadding)
    debugButton.setFocusTraversable(false)

    debugButton
  }

  private def createCompileButton: Button = {
    val compileButton = Utilities.createButton(Resources.Images.IMAGE_RUN, buttonSize, buttonPadding)
    compileButton.setFocusTraversable(false)

    compileButton
  }

  private def layoutToolbars(): Unit = {
    this.add(generalToolbar, 0, 0)
    this.add(buildToolbar, 1, 0)
    this.add(miscToolbar, 2, 0)
    this.setAlignment(Pos.CENTER_RIGHT)
  }

  private def assignColumnConstraints(): Unit = {
    val col1 = new ColumnConstraints
    col1.setHgrow(Priority.ALWAYS)
    col1.setFillWidth(false)
    col1.setHalignment(HPos.LEFT)
    col1.setPrefWidth(100)

    val col2 = new ColumnConstraints
    col2.setHgrow(Priority.NEVER)

    val col3 = new ColumnConstraints
    col3.setHgrow(Priority.ALWAYS)
    col3.setHalignment(HPos.RIGHT)

    this.getColumnConstraints.addAll(col1, col2, col3)
  }

  private def createNewEditorButton: Button = {
    val newEditorButton = Utilities.createButton(Resources.Images.IMAGE_NEW, buttonSize, 5)
    newEditorButton.setId("new-button")
    newEditorButton.setFocusTraversable(false)
    newEditorButton.setOnAction((_: ActionEvent) => {
      AppController.instance().setFlowState(FlowState.NEW_RESOURCE)
      //      AppController.instance().addTab("new_file", "")
    })

    newEditorButton
  }

  private def createOpenResourceButton: SlideButton = {
    val imageOpen = new Image(Utilities.getResource(Resources.Images.IMAGE_OPEN))
    val imageOpenView = new ImageView(imageOpen)

    val openButton = new SlideButton("", imageOpenView, buttonSize)
    openButton.setId("open-button")
    openButton.setFocusTraversable(false)
    openButton.setHorizontalOrientation(false)

    imageOpenView.fitWidthProperty.bind(Bindings.subtract(openButton.widthProperty, 2))
    imageOpenView.fitHeightProperty.bind(Bindings.subtract(openButton.heightProperty, 2))
    openButton.setOnAction((_: ActionEvent) => {
      openButton.hide()

      AppController.instance().setFlowState(FlowState.OPEN_RESOURCE)
    })

    openButton
  }

  private def createSaveButton: SlideButton = {
    val imageSave = new Image(Utilities.getResource(Resources.Images.IMAGE_SAVE))
    val imageSaveView = new ImageView(imageSave)

    val saveButton = new SlideButton("", imageSaveView, buttonSize)
    saveButton.setId("save-button")
    saveButton.setFocusTraversable(false)
    saveButton.setHorizontalOrientation(false)

    imageSaveView.fitWidthProperty.bind(Bindings.subtract(saveButton.widthProperty, 2))
    imageSaveView.fitHeightProperty.bind(Bindings.subtract(saveButton.heightProperty, 2))

    val saveAllButton = Utilities.createButton(Resources.Images.IMAGE_SAVE_ALL, buttonSize, buttonPadding)
    saveAllButton.setOnAction((_: ActionEvent) => saveButton.hide())
    saveButton.setContent(saveAllButton)
    saveButton.setOnAction((_: ActionEvent) => {
      val file = MainContent.instance().getFileDialog("Save file")

      if (file != null &&
        FxDialogs.showConfirm("Overwrite file?", "Are you sure to overwrite this file?",
          FxDialogs.YES, FxDialogs.NO).equals(FxDialogs.YES)) {
        //MainContent.instance().filePath = file.getParent

        val text = AppController.instance().getCurrentTextEditor.getText
        val fileToWrite = new File(file.getCanonicalPath)
        val bw = new BufferedWriter(new FileWriter(fileToWrite))
        bw.write(text)
        bw.close()
      }
    })
    saveButton
  }

  def switchView(projectView: Boolean): Unit = {
    viewButton.setSelected(!projectView)
  }

  def activateQuickAccess(): Unit = {
    quickAccessBox.requestFocus()
  }
}

object HeaderArea {
  private var _instance: HeaderArea = _

  def instance(): HeaderArea = {
    if (_instance == null) _instance = new HeaderArea()

    _instance
  }
}