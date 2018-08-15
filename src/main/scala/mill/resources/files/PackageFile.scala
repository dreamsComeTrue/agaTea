// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.resources.files

import java.io.File

import javafx.collections.{FXCollections, ObservableList}
import mill.resources.{Project, Resource, ResourceHandler}

import scala.collection.JavaConverters._

object PackageFile {

  class PackageFileResourceHandler extends ResourceHandler {
    override def getGroupName = "Java Files"

    override def getGroupResourceName = "Package"

    override def getDefaultResourceName = ""

    override def getFileResourceExtension: String = null

    override def handleCreate(args: Any*): Resource = null

    override def handleOpen(args: Any*): Resource = null

    override def isProjectHandler = false
  }

}

class PackageFile(var project: Project, name: String, var outDirPath: String, var rawDirectory: Boolean) extends Resource(name) {
  private val packages = FXCollections.observableArrayList[PackageFile]
  private val classes = FXCollections.observableArrayList[ClassFile]

  override def getFullPath: String = {
    if ("src" == this.name) return project.getSrcDirectory + File.separatorChar
    else if ("bin" == this.name) return project.getBinDirectory + File.separatorChar
    this.outDirPath + File.separatorChar
  }

  override def setContent(content: String): Unit = {
  }

  override def getContent: String = null

  override def saveToFile(path: String): Unit = {
  }

  def isRawDirectory: Boolean = rawDirectory

  def getPackages: ObservableList[PackageFile] = packages

  def findSubPackage(name: String): PackageFile = {
    for (packageFile <- asScalaBuffer(packages)) {
      if (packageFile.getName == name) return packageFile
    }
    null
  }

  def addPackage(name: String): PackageFile = if (findSubPackage(name) == null) {
    val pf = new PackageFile(this.getProject, name, outDirPath, false)
    packages.add(pf)

    pf
  }
  else null

  def getClasses: ObservableList[ClassFile] = classes

  def getOutDirPath: String = outDirPath

  def setOutDirPath(outDirPath: String): Unit = {
    this.outDirPath = outDirPath
  }

  def getProject: Project = project

  def removeResource(value: Resource): Unit = {
    if (packages.contains(value)) packages.remove(value)
    else if (classes.contains(value)) classes.remove(value)
  }
}
