// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.model

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.{FXCollections, ObservableMap}
import mill.resources.{Project, Resource}
import org.apache.commons.io.FilenameUtils

class ProjectsRepository private() {
  private val projects = FXCollections.observableHashMap[String, Project]
  private val _activeProject: Project = null
  private val activeProject = new SimpleObjectProperty[Project](_activeProject)
  private val openFiles = FXCollections.observableHashMap[String, Resource]

  def getProjects: ObservableMap[String, Project] = projects

  def findProject(name: String): Project = projects.get(name)

  def addProject(projectName: String, project: Project): Boolean = {
    if (!projects.containsKey(projectName)) {
      projects.put(projectName, project)

      return true
    }
    false
  }

  def removeProject(projectName: String): Boolean = {
    if (projects.containsKey(projectName)) {
      projects.remove(projectName)

      return true
    }
    false
  }

  def getActiveProject: Project = activeProject.get

  def activeProjectProperty: SimpleObjectProperty[Project] = activeProject

  def setActiveProject(activeProject: Project): Unit = {
    this.activeProject.set(activeProject)
  }

  def getOpenFiles: ObservableMap[String, Resource] = openFiles

  def closeOpenFile(filePath: String): Unit = {
    openFiles.remove(FilenameUtils.normalize(filePath))
  }

  def clearAllOpenFiles(): Unit = {
    openFiles.clear()
  }

  def addOpenedFile(filePath: String, resource: Resource): Boolean = {
    val normalFilePath = FilenameUtils.normalize(filePath)

    if (!openFiles.containsKey(normalFilePath)) {
      openFiles.put(normalFilePath, resource)
      return true
    }

    false
  }

  def isFileOpened(filePath: String): Boolean = openFiles.containsKey(FilenameUtils.normalize(filePath))

  def getOpenedFile(filePath: String): Resource = openFiles.get(filePath)

  def isProjectOpened(filePath: String): Boolean = projects.containsKey(FilenameUtils.normalize(filePath))

  def getOpenedProject(filePath: String): Resource = projects.get(filePath)
}


object ProjectsRepository {
  private var _instance: ProjectsRepository = _

  def instance(): ProjectsRepository = {
    if (_instance == null) _instance = new ProjectsRepository

    _instance
  }
}
