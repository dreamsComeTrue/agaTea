// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.model

import mill.resources.{Project, Resource}
import org.apache.commons.io.FilenameUtils
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableHashMap

class ProjectsRepository private() {
  private val projects = new ObservableHashMap[String, Project]()
  private val _activeProject: Project = null
  private val activeProject = new ObjectProperty[Project](_activeProject, "")
  private val openFiles = new ObservableHashMap[String, Resource]()

  def getProjects: ObservableHashMap[String, Project] = projects

  def findProject(name: String): Project = projects(name)

  def addProject(projectName: String, project: Project): Boolean = {
    if (!projects.containsKey(projectName)) {
      projects.put(projectName, project)

      true
    } else {
      false
    }
  }

  def removeProject(projectName: String): Boolean = {
    if (projects.containsKey(projectName)) {
      projects.remove(projectName)

      true
    } else {
      false
    }
  }

  def getActiveProject: Project = activeProject.get

  def activeProjectProperty: ObjectProperty[Project] = activeProject

  def setActiveProject(activeProject: Project): Unit = {
    this.activeProject.set(activeProject)
  }

  def getOpenFiles: ObservableHashMap[String, Resource] = openFiles

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
      true
    } else {
      false
    }
  }

  def isFileOpened(filePath: String): Boolean = openFiles.containsKey(FilenameUtils.normalize(filePath))

  def getOpenedFile(filePath: String): Resource = openFiles(filePath)

  def isProjectOpened(filePath: String): Boolean = projects.containsKey(FilenameUtils.normalize(filePath))

  def getOpenedProject(filePath: String): Resource = projects(filePath)
}


object ProjectsRepository {
  private var _instance: ProjectsRepository = _

  def instance(): ProjectsRepository = {
    if (_instance == null) _instance = new ProjectsRepository

    _instance
  }
}
