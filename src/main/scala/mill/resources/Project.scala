// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.resources

import javafx.beans.property.SimpleObjectProperty
import mill.resources.files.PackageFile
import mill.resources.settings.ProjectSettings
import scalafx.collections.ObservableBuffer

class Project(name: String) extends Resource(name) {
  protected var fullPath: String = _
  protected var binDirectory: String = _
  protected var srcDirectory: String = _
  protected var packageFiles = new ObservableBuffer[PackageFile]()
  protected var projectSettings = new SimpleObjectProperty[ProjectSettings](new ProjectSettings)

  def addPackageFile(packageFile: PackageFile): Unit = {
    packageFiles += packageFile
  }

  def findSubPackage(name: String): Option[PackageFile] = packageFiles.find((packageFile: PackageFile) => packageFile.getName == name)

  def getPackageFiles: ObservableBuffer[PackageFile] = packageFiles

  /**
    * Gets path to file on disk
    *
    * @return path to file on disk
    */
  override def getFullPath: String = fullPath

  override def setContent(content: String): Unit = {
  }

  override def getContent: String = null

  override def saveToFile(path: String): Unit = {
  }

  def getSrcDirectory: String = srcDirectory

  def getBinDirectory: String = binDirectory

  def getProjectSettings: ProjectSettings = projectSettings.get
}
