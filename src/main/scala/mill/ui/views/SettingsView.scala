// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.views

import java.{lang, util}

import javafx.beans.binding.{Bindings, BooleanBinding}
import javafx.beans.property.{ReadOnlyStringWrapper, SimpleObjectProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.{ListChangeListener, ObservableList}
import javafx.css.PseudoClass
import javafx.event.ActionEvent
import javafx.geometry.{Insets, Pos}
import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout._
import javafx.scene.paint.Color
import mill.controller.AppController
import mill.model.{ActionShortcut, ProjectsRepository}
import mill.resources.Project
import mill.resources.settings.{ApplicationSettings, ProjectSettings}
import mill.ui.controls.{PercentageTreeTableView, SearchBox}
import mill.{Resources, Utilities}
import org.controlsfx.tools.Borders

import scala.collection.JavaConverters._

class SettingsView private() extends  BorderPane {
  private var centerPane: StackPane = _
  private var editorPane: AnchorPane = _
  private var compilerPane: GridPane = _
  private var keysPane: GridPane = _
  private var projectSettingsPane: GridPane = _
  private var indexTree: TreeView[String] = _
  private var projectSettings: TreeItem[String] = _

  init()

  private def init(): Unit = {
    val topBar: VBox = prepareTopBar

    createCenterPane()

    val bottomRowWithBorder: Node = createBottomRow

    this.setCenter(centerPane)
    this.setTop(topBar)
    this.setLeft(prepareNavigationTree)
    this.setBottom(bottomRowWithBorder)
  }

  private def createCenterPane(): Unit = {
    editorPane = prepareEditorPane
    compilerPane = prepareCompilerPane
    keysPane = prepareKeysPane
    projectSettingsPane = prepareProjectSettingsPane

    StackPane.setMargin(editorPane, new Insets(5, 5, 5, 15))
    StackPane.setMargin(compilerPane, new Insets(5, 5, 5, 15))
    StackPane.setMargin(keysPane, new Insets(5, 5, 5, 15))
    StackPane.setMargin(projectSettingsPane, new Insets(5, 5, 5, 15))

    centerPane = new StackPane(editorPane, compilerPane, keysPane, projectSettingsPane)
  }

  private def createBottomRow: Node = {
    val saveButton: Button = createSaveButton
    val cancelButton: Button = createCancelButton
    val bottomRow: AnchorPane = new AnchorPane(saveButton, cancelButton)

    AnchorPane.setRightAnchor(saveButton, 30.0)
    AnchorPane.setRightAnchor(cancelButton, 5.0)

    Borders.wrap(bottomRow).lineBorder.innerPadding(0).outerPadding(0).color(Color.rgb(50, 50, 50), Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT).buildAll
  }

  private def prepareTopBar: VBox = {
    val headerLabel: Label = new Label(Resources.SETTINGS)
    val topBar: VBox = new VBox(headerLabel)

    topBar.setAlignment(Pos.CENTER)
    topBar.getStyleClass.addAll("window-header")
    topBar
  }

  private def createCancelButton: Button = {
    val cancelButton: Button = Utilities.createButton(Resources.Images.IMAGE_REJECT, 20, 3)
    cancelButton.setOnAction((_: ActionEvent) => AppController.instance().switchToLastState())
    cancelButton
  }

  private def createSaveButton: Button = {
    val saveButton: Button = Utilities.createButton(Resources.Images.IMAGE_ACCEPT, 20, 3)
    saveButton.setOnAction((_: ActionEvent) => AppController.instance().switchToLastState())
    saveButton
  }

  private def prepareNavigationTree: VBox = {
    val searchBox: SearchBox = new SearchBox
    searchBox.setMinHeight(20.0)
    searchBox.setPromptText(Resources.FIND)

    val editorSettings: TreeItem[String] = new TreeItem[String](Resources.EDITOR)
    val compilerSettings: TreeItem[String] = new TreeItem[String]("Compiler")
    val keymapSettings: TreeItem[String] = new TreeItem[String]("Keys")

    projectSettings = new TreeItem[String]("Project Settings")

    val rootItem: TreeItem[String] = new TreeItem[String]
    rootItem.getChildren.addAll(editorSettings, compilerSettings, keymapSettings, projectSettings)

    indexTree = new TreeView[String](rootItem)
    indexTree.setCellFactory((_: TreeView[String]) => new SettingsTreeCellImpl)
    indexTree.setShowRoot(false)
    indexTree.getStyleClass.add("tree-view-no-border")
    indexTree.setPrefWidth(100)
    indexTree.setMinHeight(0.0)

    val leftBox: VBox = new VBox(searchBox, indexTree)
    VBox.setMargin(searchBox, new Insets(2, 2, 2, 2))
    leftBox.setStyle("-fx-border-color: transparent rgb(50, 50, 50) transparent transparent;")

    indexTree.getSelectionModel.selectedItemProperty.addListener(new ChangeListener[TreeItem[String]] {
      override def changed(observableValue: ObservableValue[_ <: TreeItem[String]], t: TreeItem[String], newValue: TreeItem[String]): Unit = {
        val value: String = newValue.getValue
        value match {
          case "Editor" => hideAllTabsExcept(editorPane)
          case "Compiler" => hideAllTabsExcept(compilerPane)
          case "Keys" => hideAllTabsExcept(keysPane)
          case "Project Settings" => hideAllTabsExcept(projectSettingsPane)
        }
      }
    })

    indexTree.getSelectionModel.selectFirst()

    leftBox
  }

  private def prepareEditorPane: AnchorPane = {
    val syntaxHighlighter: CheckBox = new CheckBox(Resources.ENABLE_DISABLE_SYNTAX_HIGHLIGHTING)
    syntaxHighlighter.setSelected(ApplicationSettings.instance().getSyntaxHighlightingEnabled)
    syntaxHighlighter.setOnAction((_: ActionEvent) => ApplicationSettings.instance().setSyntaxHighlightingEnabled(syntaxHighlighter.isSelected))

    ApplicationSettings.instance().syntaxHighlightingEnabledProperty.addListener(new ChangeListener[lang.Boolean] {
      override def changed(observableValue: ObservableValue[_ <: lang.Boolean], t: lang.Boolean, newValue: lang.Boolean): Unit = {
        syntaxHighlighter.setSelected(newValue)
      }
    })

    val highlightCurrentLine: CheckBox = new CheckBox(Resources.HIGHLIGHT_CURRENT_LINE)
    highlightCurrentLine.setSelected(ApplicationSettings.instance().getHighlightCurrentLine)
    highlightCurrentLine.setOnAction((_: ActionEvent) => ApplicationSettings.instance().setHighlightCurrentLine(highlightCurrentLine.isSelected))

    ApplicationSettings.instance().highlightCurrentLineProperty.addListener(new ChangeListener[lang.Boolean] {
      override def changed(observableValue: ObservableValue[_ <: lang.Boolean], t: lang.Boolean, newValue: lang.Boolean): Unit = {
        highlightCurrentLine.setSelected(newValue)
      }
    })

    val lineNumbers: CheckBox = new CheckBox(Resources.SHOW_LINE_NUMBERS)
    lineNumbers.setSelected(ApplicationSettings.instance().getLineNumbersVisible)
    lineNumbers.setOnAction((_: ActionEvent) => ApplicationSettings.instance().setLineNumbersVisible(lineNumbers.isSelected))

    ApplicationSettings.instance().lineNumbersVisibleProperty.addListener(new ChangeListener[lang.Boolean] {
      override def changed(observableValue: ObservableValue[_ <: lang.Boolean], t: lang.Boolean, newValue: lang.Boolean): Unit = {
        lineNumbers.setSelected(newValue)
      }
    })

    val autocompletePairedChars: CheckBox = new CheckBox(Resources.AUTOCOMPLETE_PAIRED_CHARS)
    autocompletePairedChars.setSelected(ApplicationSettings.instance().getAutocompletePairedChars)
    autocompletePairedChars.setOnAction((_: ActionEvent) => ApplicationSettings.instance().setAutocompletePairedChars(autocompletePairedChars.isSelected))

    ApplicationSettings.instance().autocompletePairedCharsProperty.addListener(new ChangeListener[lang.Boolean] {
      override def changed(observableValue: ObservableValue[_ <: lang.Boolean], t: lang.Boolean, newValue: lang.Boolean): Unit = {
        autocompletePairedChars.setSelected(newValue)
      }
    })

    val defaultFontSize: Label = new Label(Resources.DEFAULT_FONT_SIZE)
    val fontSizeSpinner: Spinner[Integer] = new Spinner[Integer](0, 40, 15)
    fontSizeSpinner.setPrefWidth(70.0)

    val defaultFontSizePane: HBox = new HBox(defaultFontSize, fontSizeSpinner)
    defaultFontSizePane.setAlignment(Pos.CENTER)
    HBox.setMargin(fontSizeSpinner, new Insets(0, 0, 0, 25.0))
    VBox.setMargin(defaultFontSizePane, new Insets(0, 0, 0, -20.0))

    val editorPane: VBox = new VBox(syntaxHighlighter, highlightCurrentLine, lineNumbers, autocompletePairedChars, defaultFontSizePane)
    editorPane.setSpacing(5.0)
    AnchorPane.setLeftAnchor(editorPane, 0.0)

    new AnchorPane(editorPane)
  }

  private def prepareCompilerPane: GridPane = {
    val gridPane: GridPane = new GridPane
    val jdkPathLabel: Label = new Label(Resources.JDK_PATH)
    val jdkPathField: TextField = new TextField
    jdkPathField.setText(ApplicationSettings.instance().getJdkPath)
    jdkPathField.textProperty.addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], t: String, newValue: String): Unit = {
        ApplicationSettings.instance().setJdkPath(newValue)
      }
    })

    val jdkButton: Button = new Button("...")
    jdkPathLabel.setLabelFor(jdkPathField)
    gridPane.add(jdkPathLabel, 0, 0)
    gridPane.add(jdkPathField, 1, 0)
    gridPane.add(jdkButton, 2, 0)

    ApplicationSettings.instance().jdkPathProperty.addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], t: String, newValue: String): Unit = {
        jdkPathField.setText(newValue)
      }
    })

    val vmParametersLabel: Label = new Label(Resources.VM_PARAMETERS)
    val vmParametersField: TextField = new TextField
    vmParametersField.setText(ApplicationSettings.instance().getVmParameters)
    vmParametersField.textProperty.addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], t: String, newValue: String): Unit = {
        ApplicationSettings.instance().setVmParameters(newValue)
      }
    })

    gridPane.add(vmParametersLabel, 0, 1)
    gridPane.add(vmParametersField, 1, 1, 2, 1)

    ApplicationSettings.instance().vmParametersProperty.addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], t: String, newValue: String): Unit = {
        vmParametersField.setText(newValue)
      }
    })

    val column1: ColumnConstraints = new ColumnConstraints
    val column2: ColumnConstraints = new ColumnConstraints
    column2.setHgrow(Priority.ALWAYS)

    val column3: ColumnConstraints = new ColumnConstraints
    gridPane.setHgap(5.0)
    gridPane.setVgap(5.0)
    gridPane.getColumnConstraints.addAll(column1, column2, column3)

    gridPane
  }

  private def prepareKeysPane: GridPane = {
    val gridPane: GridPane = new GridPane
    val searchField: TextField = new TextField
    searchField.setPromptText(Resources.SEARCH)

    gridPane.add(searchField, 0, 0)
    GridPane.setColumnSpan(searchField, 2)

    val keysBindingsRoot: TreeItem[ActionShortcut] = new TreeItem[ActionShortcut]
    val actionsList: util.List[TreeItem[ActionShortcut]] = ActionShortcut.actionsList.map(a => new TreeItem[ActionShortcut](a)).toList.asJava

    ActionShortcut.actionsDirtyProperty.addListener(new ChangeListener[lang.Boolean] {
      override def changed(observableValue: ObservableValue[_ <: lang.Boolean], t: lang.Boolean, newValue: lang.Boolean): Unit = {
        if (newValue) {
          actionsList.clear()
          keysBindingsRoot.getChildren.clear()

          actionsList.addAll(ActionShortcut.actionsList.map(a => new TreeItem[ActionShortcut](a)).toList.asJava)
          keysBindingsRoot.getChildren.addAll(actionsList)

          ActionShortcut.setActionsDirty(false)
        }
      }
    })

    keysBindingsRoot.getChildren.addAll(actionsList)

    val keyBindingsTreeTable: PercentageTreeTableView[ActionShortcut] = new PercentageTreeTableView[ActionShortcut]
    keyBindingsTreeTable.getTreeTableView.setShowRoot(false)
    keyBindingsTreeTable.getTreeTableView.setRoot(keysBindingsRoot)

    val actionNameColumn: PercentageTreeTableView.PercentageTableColumn[ActionShortcut, String] = new PercentageTreeTableView.PercentageTableColumn[ActionShortcut, String]("Action")
    actionNameColumn.setPercentWidth(70)

    val shortcutColumn: PercentageTreeTableView.PercentageTableColumn[ActionShortcut, String] = new PercentageTreeTableView.PercentageTableColumn[ActionShortcut, String]("Shortcut")
    shortcutColumn.setPercentWidth(30)

    keyBindingsTreeTable.getTreeTableView.getColumns.addAll(actionNameColumn, shortcutColumn)

    actionNameColumn.setCellValueFactory((p: TreeTableColumn.CellDataFeatures[ActionShortcut, String]) => new ReadOnlyStringWrapper(p.getValue.getValue.getDescription))

    shortcutColumn.setCellValueFactory((p: TreeTableColumn.CellDataFeatures[ActionShortcut, String]) => new ReadOnlyStringWrapper(p.getValue.getValue.getKeyCombination))
    GridPane.setColumnSpan(keyBindingsTreeTable, 2)
    gridPane.add(keyBindingsTreeTable, 0, 1)

    val column1: ColumnConstraints = new ColumnConstraints
    val column2: ColumnConstraints = new ColumnConstraints
    column2.setHgrow(Priority.ALWAYS)

    gridPane.setHgap(5.0)
    gridPane.setVgap(5.0)
    gridPane.getColumnConstraints.addAll(column1, column2)

    gridPane
  }

  private def prepareProjectSettingsPane: GridPane = {
    val activeProject: SimpleObjectProperty[Project] = ProjectsRepository.instance().activeProjectProperty
    val activeProjectObj: Project = activeProject.get
    val gridPane: GridPane = new GridPane

    val topGridColumn1: ColumnConstraints = new ColumnConstraints
    val topGridColumn2: ColumnConstraints = new ColumnConstraints
    topGridColumn2.setHgrow(Priority.ALWAYS)

    val topGridPane: GridPane = new GridPane
    topGridPane.setVgap(5.0)
    topGridPane.getColumnConstraints.addAll(topGridColumn1, topGridColumn2)

    val mainClassLabel: Label = new Label(Resources.MAIN_CLASS)
    val mainClassLabelField: TextField = new TextField

    if (activeProjectObj != null) {
      mainClassLabelField.setText(activeProjectObj.getProjectSettings.getMainClass)

      activeProjectObj.getProjectSettings.mainClassProperty.addListener(new ChangeListener[String] {
        override def changed(observableValue: ObservableValue[_ <: String], t: String, newValue: String): Unit = {
          mainClassLabelField.setText(newValue)
        }
      })
    }

    mainClassLabelField.textProperty.addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], t: String, newValue: String): Unit = {
        if (activeProjectObj != null) activeProjectObj.getProjectSettings.setMainClass(newValue)
      }
    })

    topGridPane.add(mainClassLabel, 0, 0)
    topGridPane.add(mainClassLabelField, 1, 0, 2, 1)

    val vmParametersLabel: Label = new Label(Resources.VM_PARAMETERS)
    val vmParametersField: TextField = new TextField
    vmParametersField.setText(ApplicationSettings.instance().getVmParameters)
    vmParametersField.textProperty.addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], t: String, newValue: String): Unit = {
        ApplicationSettings.instance().setVmParameters(newValue)
      }
    })

    topGridPane.add(vmParametersLabel, 0, 1)
    topGridPane.add(vmParametersField, 1, 1, 2, 1)

    ApplicationSettings.instance().vmParametersProperty.addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], t: String, newValue: String): Unit = {
        vmParametersField.setText(newValue)
      }
    })

    gridPane.add(topGridPane, 0, 0, 2, 1)

    val additionalDependenciesLabel: Label = new Label(Resources.ADDITIONAL_DEPENDENCIES)
    val addButton: Button = new Button("", Utilities.createImageView(Resources.Images.IMAGE_ADD, 11))
    val removeButton: Button = new Button("", Utilities.createImageView(Resources.Images.IMAGE_REMOVE, 11))

    val dependenciesButtonsBox: HBox = new HBox(addButton, removeButton)
    topGridPane.add(additionalDependenciesLabel, 0, 2)
    topGridPane.add(dependenciesButtonsBox, 1, 2)
    dependenciesButtonsBox.setAlignment(Pos.CENTER_RIGHT)

    val dependenciesRoot: TreeItem[String] = new TreeItem[String]
    dependenciesRoot.getChildren.add(new TreeItem[String]("library1"))
    dependenciesRoot.getChildren.add(new TreeItem[String]("library2"))
    dependenciesRoot.getChildren.add(new TreeItem[String]("library3"))

    val dependenciesTreeTable: PercentageTreeTableView[String] = new PercentageTreeTableView[String]
    dependenciesTreeTable.getTreeTableView.setShowRoot(false)
    dependenciesTreeTable.getTreeTableView.setRoot(dependenciesRoot)

    val pathColumn: PercentageTreeTableView.PercentageTableColumn[String, String] = new PercentageTreeTableView.PercentageTableColumn[String, String]("Path")
    pathColumn.setPercentWidth(70)

    val nameColumn: PercentageTreeTableView.PercentageTableColumn[String, String] = new PercentageTreeTableView.PercentageTableColumn[String, String]("Name")
    nameColumn.setPercentWidth(30)
    dependenciesTreeTable.getTreeTableView.getColumns.addAll(pathColumn, nameColumn)
    pathColumn.setCellValueFactory((p: TreeTableColumn.CellDataFeatures[String, String]) => new ReadOnlyStringWrapper(p.getValue.getValue))
    nameColumn.setCellValueFactory((p: TreeTableColumn.CellDataFeatures[String, String]) => new ReadOnlyStringWrapper(p.getValue.getValue))
    gridPane.add(dependenciesTreeTable, 0, 1, 2, 1)

    val column1: ColumnConstraints = new ColumnConstraints
    val column2: ColumnConstraints = new ColumnConstraints
    column2.setHgrow(Priority.ALWAYS)

    gridPane.setHgap(5.0)
    gridPane.setVgap(5.0)
    gridPane.getColumnConstraints.addAll(column1, column2)

    activeProject.addListener(new ChangeListener[Project] {
      override def changed(observableValue: ObservableValue[_ <: Project], t: Project, newValue: Project): Unit = {
        val settings: ProjectSettings = newValue.getProjectSettings

        settings.mainClassProperty.addListener(new ChangeListener[String] {
          override def changed(observableValue: ObservableValue[_ <: String], t: String, newValue1: String): Unit = {
            mainClassLabelField.setText(newValue1)
          }
        })

        settings.vmArgumentsProperty.addListener(new ChangeListener[String] {
          override def changed(observableValue: ObservableValue[_ <: String], t: String, newValue1: String): Unit = {
            vmParametersField.setText(newValue1)
          }
        })

        settings.getDependencies.addListener(new ListChangeListener[String] {
          override def onChanged(change: ListChangeListener.Change[_ <: String]): Unit = {
            val dependencies: ObservableList[String] = settings.getDependencies
            dependenciesRoot.getChildren.clear()

            for (dependency <- dependencies.asScala) {
              dependenciesRoot.getChildren.add(new TreeItem[String](dependency))
            }
          }
        })

        mainClassLabelField.setText(settings.getMainClass)
        vmParametersField.setText(settings.getVmArguments)

        val dependencies: ObservableList[String] = settings.getDependencies
        dependenciesRoot.getChildren.clear()

        for (dependency <- dependencies.asScala) {
          dependenciesRoot.getChildren.add(new TreeItem[String](dependency))
        }
      }
    })

    gridPane
  }

  private def hideAllTabsExcept(tab: Node): Unit = {
    for (i <- 0 until centerPane.getChildren.size()) {
      val node = centerPane.getChildren.get(i)

      if (tab != node) {
        node.toBack()
        node.setVisible(false)
      }
    }

    tab.setVisible(true)
    tab.toFront()
  }

  def keyPressed(event: KeyEvent): Unit = {
    if (event.getCode == KeyCode.ESCAPE) AppController.instance().switchToLastState()
  }

  def openProjectSettingsPage(): Unit = {
    indexTree.getSelectionModel.select(projectSettings)
  }

  final private val highlighted: PseudoClass = PseudoClass.getPseudoClass("highlighted")

  final private class SettingsTreeCellImpl() extends TreeCell[String] {
    private val label: Label = new Label

    // indicates whether the cell should be highlighted:
    private val highlightCell: BooleanBinding = Bindings.createBooleanBinding(() => getTreeItem != null && indexTree.getSelectionModel.getSelectedItems.contains(getTreeItem), treeItemProperty, indexTree.getSelectionModel.getSelectedItems)

    // listener for the binding above
    // note this has to be scoped to persist alongside the cell, as the binding
    // will use weak listeners, and we need to avoid the listener getting gc'd:
    private val listener: ChangeListener[lang.Boolean] = (_: ObservableValue[_ <: lang.Boolean], _: lang.Boolean, isHighlighted: lang.Boolean) => pseudoClassStateChanged(highlighted, isHighlighted)

    highlightCell.addListener(listener)

    override protected def updateItem(item: String, empty: Boolean): Unit = {
      super.updateItem(item, empty)

      label.setText(item)
      setGraphic(label)
    }
  }

}

object SettingsView {
  private var _instance: SettingsView = _

  def instance(): SettingsView = {
    if (_instance == null) _instance = new SettingsView()

    _instance
  }
}
