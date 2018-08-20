// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.views.explorer

import javafx.collections.{MapChangeListener, ObservableMap}
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control._
import javafx.scene.layout.{AnchorPane, BorderPane, VBox}
import javafx.scene.paint.Color
import mill.controller.{AppController, FlowState, GlobalState}
import mill.model.ProjectsRepository
import mill.resources.files.{ClassFile, PackageFile}
import mill.resources.settings.ApplicationSettings
import mill.resources.{Project, Resource}
import mill.ui.views.SettingsView
import mill.{Resources, Utilities}
import org.apache.commons.io.FilenameUtils
import org.controlsfx.tools.Borders
import scalafx.collections.ObservableBuffer

import scala.collection.mutable.ListBuffer


class ProjectExplorer private() extends BorderPane {
  private val stackedTitledPanes = new VBox
  private val globalScroll = new ScrollPane
  private var recentFileEntry: RecentFileEntry = _
  private val projectEntries = ListBuffer[ProjectEntry]()
  private var currentProjectEntry: ProjectEntry = _
  private var selectedTreeItems = new ObservableBuffer[TreeItem[Resource]]()
  private var selectedItems = new ObservableBuffer[Resource]()

  init()

  private def init(): Unit = {
    val buttonsRow: Node = createButtonsRow

    this.getStyleClass.add("project-explorer-background")
    this.setTop(buttonsRow)
    this.setMinWidth(0.0)

    stackedTitledPanes.getStyleClass.addAll("project-explorer-no-border")

    globalScroll.setContent(stackedTitledPanes)
    globalScroll.setFitToWidth(true)
    globalScroll.getStyleClass.addAll("project-explorer-no-border")

    val borderColor: Color = Color.rgb(50, 50, 50)
    val outline: Node = Borders.wrap(globalScroll).lineBorder.innerPadding(0).outerPadding(0).color(Color.TRANSPARENT, borderColor, Color.TRANSPARENT, Color.TRANSPARENT).buildAll
    BorderPane.setMargin(outline, new Insets(0, 0, 0, -7))

    this.setCenter(outline)

    addProjectsListener()
  }

  def initialize(): Unit = {
    //	Add missing project non categorized
    recentFileEntry = new RecentFileEntry

    val projectPane: TitledPane = recentFileEntry.getProjectPane
    VBox.setMargin(projectPane, new Insets(0, -5, 0, 0))

    val openFiles: ObservableMap[String, Resource] = ProjectsRepository.instance().getOpenFiles

    openFiles.addListener(new MapChangeListener[String, Resource] {
      override def onChanged(change: MapChangeListener.Change[_ <: String, _ <: Resource]): Unit = {
        if (openFiles.size > 0) {
          val children = stackedTitledPanes.getChildren

          if (children.size == 0) stackedTitledPanes.getChildren.add(0, projectPane)
          else if (children.get(0) != projectPane) stackedTitledPanes.getChildren.add(0, projectPane)
        }
        else stackedTitledPanes.getChildren.remove(projectPane)
      }
    })
  }

  private def createButtonsRow: Node = {
    val expandCollapseButton: ToggleButton = createExpandCollapseButton
    val toolsButton: Button = createToolsButton
    val stickyButton: ToggleButton = createStickyButton
    val buttonsRow: AnchorPane = new AnchorPane

    AnchorPane.setRightAnchor(expandCollapseButton, 0.0)
    AnchorPane.setRightAnchor(stickyButton, 20.0)
    buttonsRow.getChildren.addAll(toolsButton, stickyButton, expandCollapseButton)
    buttonsRow.getStyleClass.add("background")

    Borders.wrap(buttonsRow).lineBorder.outerPadding(0).innerPadding(0).color(Color.rgb(50, 50, 50)).buildAll
  }

  private def addProjectsListener(): Unit = {
    ProjectsRepository.instance().getProjects.addListener(new MapChangeListener[String, Project] {
      override def onChanged(change: MapChangeListener.Change[_ <: String, _ <: Project]): Unit = {
        if (change.wasAdded) {
          val projectAdded: Project = change.getValueAdded
          addProject(projectAdded)
        }
        else if (change.wasRemoved) {
          val projectRemoved: Project = change.getValueRemoved
          removeProject(projectRemoved)
        }
      }
    })
  }

  /**
    * Adds new project to view
    *
    * @param project project to add
    */
  private def addProject(project: Project): Unit = {
    val projectEntry: ProjectEntry = new ProjectEntry(Resources.Images.IMAGE_WINDOW, project.getName, project)
    projectEntries += projectEntry

    //     project.getPackageFiles ().addListener ((ListChangeListener<PackageFile>) c ->
    //             addProjectItem (projectItem, c.getAddedSubList ().get (0)));

    val projectPane: TitledPane = projectEntry.getProjectPane

    VBox.setMargin(projectPane, new Insets(0, -5, 0, 0))

    stackedTitledPanes.getChildren.add(projectPane)
    currentProjectEntry = projectEntry
  }

  /**
    * Removes project from view
    *
    * @param project project to remove
    */
  private def removeProject(project: Project): Unit = {
    var entryToRemove: ProjectEntry = null

    for (entry <- projectEntries) {
      if (entry.getProject == project) {
        entryToRemove = entry
        stackedTitledPanes.getChildren.remove(entry.getProjectPane)
      }
    }

    projectEntries -= entryToRemove
    currentProjectEntry = null
  }

  private def createToolsButton: Button = {
    val toolsButton: Button = Utilities.createButton(Resources.Images.IMAGE_TOOLS, 20, Utilities.DEFAULT_IMAGE_PADDING)
    toolsButton.setFocusTraversable(false)

    toolsButton.setOnAction((_: ActionEvent) => {
      AppController.instance().setFlowState(FlowState.SETTINGS)
      SettingsView.instance().openProjectSettingsPage()
    })

    toolsButton
  }

  private def createExpandCollapseButton: ToggleButton = {
    val expandCollapseButton: ToggleButton = Utilities.createOnOffButton(Resources.Images.IMAGE_COLLAPSE, Resources.Images.IMAGE_EXPAND, 20, 4)
    expandCollapseButton.setFocusTraversable(false)
    expandCollapseButton
  }

  private def createStickyButton: ToggleButton = {
    val stickyButton: ToggleButton = Utilities.createOnOffButton(Resources.Images.MAGNET_OFF, Resources.Images.MAGNET_ON, 20, 4)
    stickyButton.setFocusTraversable(false)
    stickyButton.setOnAction((_: ActionEvent) => ApplicationSettings.instance().setStickyProjectExplorer(stickyButton.isSelected))
    stickyButton.setSelected(ApplicationSettings.instance().getStickyProjectExplorer)

    stickyButton
  }

  def getGlobalScroll: ScrollPane = globalScroll

  def getCurrentProjectEntry: ProjectEntry = currentProjectEntry

  /**
    * Tries to add package with given name
    *
    * @param add         true for adding, false for validating
    * @param packageName name of the new package to add
    * @return true if package added successfully
    */
  def addPackageToProject(add: Boolean, packageName: String): Boolean = {
    val project: Project = ProjectsRepository.instance().getActiveProject
    val packageFile: PackageFile = new PackageFile(project, packageName, packageName, false)

    if (selectedTreeItems.size > 0) {
      for (i <- 0 until selectedTreeItems.size()) {
        val selectedTreeItem: TreeItem[Resource] = selectedTreeItems.get(i)

        selectedTreeItem.getValue match {
          case selectedPackage: PackageFile =>

            if (selectedPackage.findSubPackage(packageName) == null) {
              if (add) {
                selectedPackage.getPackages.add(packageFile)
                return true
              }
            }
            else {
              return false
            }
          case _ =>
        }
      }
    }
    else {
      if (project.findSubPackage(packageName) == null) {
        if (add) project.addPackageFile(packageFile)
      }
      else return false
    }

    true
  }

  def openResource(res: Resource): Unit = {
    if (res != null) {
      val path: String = FilenameUtils.normalize(res.getFullPath)

      //  Try to open resource file in new editor
      if (!GlobalState.instance().isFileOpened(path)) AppController.instance().openResourceInEditor(res.getName, path, res)

      AppController.instance().focusEditor(path)
    }
  }

  /**
    * Closes editor window with given name
    *
    * @param path of the file to close
    */
  def closeResourceInEditor(path: String): Unit = {
    AppController.instance().closeResourceInEditor(path)
  }

  /**
    * Recursively closes all resources in selected project
    *
    * @param project all resources to close
    */
  def closeAllResourcesInEditor(project: Project): Unit = {
    projectEntries.filter((entry: ProjectEntry) => entry.getProject eq project).foreach((entry: ProjectEntry) => {
      for (packageFile: PackageFile <- project.getPackageFiles) {
        for (classFile: ClassFile <- packageFile.getClasses) {
          closeResourceInEditor(classFile.getFullPath)
        }
      }
    })
  }

  def getSelectedTreeItems: ObservableBuffer[TreeItem[Resource]] = selectedTreeItems

  def setSelectedTreeItems(selectedTreeItems: ObservableBuffer[TreeItem[Resource]]): Unit = {
    this.selectedTreeItems = selectedTreeItems
  }

  def getSelectedItems: ObservableBuffer[Resource] = selectedItems

  def setSelectedItems(selectedItems: ObservableBuffer[Resource]): Unit = {
    this.selectedItems = selectedItems
  }

  def getProjectEntries: ListBuffer[ProjectEntry] = projectEntries

  def getRecentFileEntry: RecentFileEntry = recentFileEntry

  override def requestFocus(): Unit = {
    if (currentProjectEntry != null) currentProjectEntry.getTree.requestFocus()
    else if (recentFileEntry.getOpenedResourcesListView.getItems.size > 0) recentFileEntry.getOpenedResourcesListView.requestFocus()
  }

  //	public class PackageEntry
  //	{
  //		public PackageEntry (PackageFile file)
  //		{
  //			Tooltip packagePanelTip = new Tooltip ("agaNewProject/main/src");
  //
  //			ImageView packageImage = Utilities.createImageView (Resources.Images.IMAGE_PACKAGE, 15);
  //			Button closePackageButton = Utilities.createButton (Resources.Images.IMAGE_ARROW_LEFT, 15, 0);
  //			closePackageButton.setStyle ("-fx-border-color: rgb(50, 50, 50);");
  //
  //			//		Label titleLabel = new Label (packageFile.getName ());
  //			//		titleLabel.setStyle ("-fx-text-fill: white;");
  //
  ////			packagePane1.getStyleClass ().addAll ("project-item-no-border");
  //		}
  //	}
}

object ProjectExplorer {
  private var _instance: ProjectExplorer = _

  def instance(): ProjectExplorer = {
    if (_instance == null) _instance = new ProjectExplorer()

    _instance
  }
}
