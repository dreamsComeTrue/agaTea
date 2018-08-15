// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.resources

import java.util.Optional

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.{FXCollections, ObservableList}
import mill.resources.files.PackageFile
import mill.resources.settings.ProjectSettings

class Project(name: String) extends Resource(name) {
  protected var fullPath: String = _
  protected var binDirectory: String = _
  protected var srcDirectory: String = _
  protected var packageFiles: ObservableList[PackageFile] = FXCollections.observableArrayList[PackageFile]
  protected var projectSettings = new SimpleObjectProperty[ProjectSettings](new ProjectSettings)

  def addPackageFile(packageFile: PackageFile): Unit = {
    packageFiles.add(packageFile)
  }

  def findSubPackage(name: String): Optional[PackageFile] = packageFiles.stream.filter((packageFile: PackageFile) => packageFile.getName == name).findFirst

  def getPackageFiles: ObservableList[PackageFile] = packageFiles

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
