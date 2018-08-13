// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import java.io.{BufferedWriter, File, FileWriter}
import java.lang

import javafx.beans.binding.Bindings
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.ActionEvent
import javafx.geometry.{HPos, Insets, Pos}
import javafx.scene.control.{Button, ComboBox, ToggleButton, Tooltip}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.input.KeyEvent
import javafx.scene.layout._
import mill.controller.AppController
import mill.ui.controls.{SearchBox, SlideButton}
import mill.{FxDialogs, Resources, Utilities}

class HeaderPane(val mainFrame: MainContent) extends GridPane {
  private val buttonSize = 25
  private val buttonPadding = 6

  private var viewButton: ToggleButton = _
  private var recentFilesTreeView = null
  private var confirmBar = null

  private var generalToolbar: HBox = _
  private var buildToolbar: HBox = _
  private var miscToolbar: HBox = _
  private var quickAccessBox: SearchBox = _

  init()

  def init(): Unit = {
    this.setMinHeight(45)

    createGeneralToolbar()
    createBuildToolbar ()
    createMiscToolbar()
    layoutToolbars()
    assignColumnConstraints()
  }

  private def createGeneralToolbar(): Unit = {
    val newEditorButton = createNewEditorButton
    val openButton = createOpenResourceButton
    val saveButton = createSaveButton

    generalToolbar = new HBox(newEditorButton, openButton, saveButton)
    generalToolbar.setAlignment(Pos.CENTER)
    generalToolbar.setPadding(new Insets(0, 0, 0, 15))
  }

  private def createBuildToolbar(): Unit = {
    val compileButton = createCompileButton
    val debugButton = createDebugButton
    buildToolbar = new HBox(compileButton, debugButton)
    buildToolbar.setAlignment(Pos.CENTER)
  }

  private def createDebugButton = {
    val debugButton = Utilities.createButton(Resources.Images.IMAGE_DEBUG, buttonSize, buttonPadding)
    debugButton.setFocusTraversable(false)

    debugButton
  }

  private def createCompileButton = {
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

  private def createNewEditorButton = {
    val newEditorButton = Utilities.createButton(Resources.Images.IMAGE_NEW, buttonSize, 5)
    newEditorButton.setId("new-button")
    newEditorButton.setFocusTraversable(false)
    newEditorButton.setOnAction((_: ActionEvent) => {
      AppController.instance().addTab("new_file", "")
    })

    newEditorButton
  }

  private def createOpenResourceButton = {
    val imageOpen = new Image(Utilities.getResource(Resources.Images.IMAGE_OPEN))
    val imageOpenView = new ImageView(imageOpen)
    imageOpenView.setFitHeight(10)
    imageOpenView.setFitWidth(10)

    val openButton = new SlideButton("", imageOpenView, buttonSize)
    openButton.setId("open-button")
    openButton.setFocusTraversable(false)
    openButton.setHorizontalOrientation(false)

    imageOpenView.fitWidthProperty.bind(Bindings.subtract(openButton.widthProperty, 2))
    imageOpenView.fitHeightProperty.bind(Bindings.subtract(openButton.heightProperty, 2))
    openButton.setOnAction((event: ActionEvent) => {
      openButton.hide()

      val file = mainFrame.getFileDialog("Open file")

      if (file != null) {
        mainFrame.filePath = file.getParent
        AppController.instance().addTab(file)
      }
    })

    openButton
  }

  private def createSaveButton = {
    val imageSave = new Image(Utilities.getResource(Resources.Images.IMAGE_SAVE))
    val imageSaveView = new ImageView(imageSave)
    imageSaveView.setFitHeight(20)
    imageSaveView.setFitWidth(20)

    val saveButton = new SlideButton("", imageSaveView, buttonSize)
    saveButton.setId("save-button")
    saveButton.setFocusTraversable(false)
    saveButton.setHorizontalOrientation(false)

    val saveAllButton = Utilities.createButton(Resources.Images.IMAGE_SAVE_ALL, buttonSize, buttonPadding)
    saveAllButton.setOnAction((_: ActionEvent) => saveButton.hide())
    saveButton.setContent(saveAllButton)
    saveButton.setOnAction((_: ActionEvent) => {
      val file = mainFrame.getFileDialog("Save file")

      if (file != null &&
        FxDialogs.showConfirm("Overwrite file?", "Are you sure to overwrite this file?",
          FxDialogs.YES, FxDialogs.NO).equals(FxDialogs.YES)) {
        mainFrame.filePath = file.getParent

        val text = AppController.instance().getCurrentTextEditor.getText
        val fileToWrite = new File(file.getCanonicalPath)
        val bw = new BufferedWriter(new FileWriter(fileToWrite))
        bw.write(text)
        bw.close()
      }
    })
    saveButton
  }

  private def createMiscToolbar(): Unit = {
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
    viewButton.setOnAction((event: ActionEvent) => {
      val controller = AppController.instance()

      if (viewButton.isSelected) {
        imageViewView.setImage(image3DView)
        viewButton.setTooltip(projectTooltip)
      }
      else {
        imageViewView.setImage(imageProject)
        viewButton.setTooltip(structureTooltip)
      }
    })
    viewButton.selectedProperty.addListener(new ChangeListener[lang.Boolean] {
      override def changed(observableValue: ObservableValue[_ <: lang.Boolean], oldValue: lang.Boolean, newValue: lang.Boolean): Unit = {
        if (viewButton.isSelected) imageViewView.setImage(image3DView)
        else imageViewView.setImage(imageProject)
      }
    })

    val settingsButton = Utilities.createButton(Resources.Images.IMAGE_SETTINGS, buttonSize, buttonPadding)
    settingsButton.setFocusTraversable(false)
    settingsButton.setOnAction((_: ActionEvent) => null)
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
    projectExplorerButton.setOnAction((event: ActionEvent) => {
      val controller = AppController.instance()
    })

    val outputConsoleButton = Utilities.createToggleButton(Resources.Images.IMAGE_WINDOW, buttonSize, buttonPadding)
    outputConsoleButton.setOnAction((event: ActionEvent) => {
      val controller = AppController.instance()
    })

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

    quickAccessBox = new SearchBox
    quickAccessBox.setFocusTraversable(false)
    quickAccessBox.getTextBox.setPromptText(Resources.QUICK_ACCESS)
    quickAccessBox.getTextBox.setOnKeyPressed((event: KeyEvent) => {
    })
    HBox.setMargin(quickAccessBox, new Insets(0, 10, 0, 0))

    miscToolbar = new HBox(quickAccessBox, viewButton, layoutButton, settingsButton)
    miscToolbar.setAlignment(Pos.CENTER_RIGHT)
    miscToolbar.setPadding(new Insets(0, 15, 0, 0))
  }
}
