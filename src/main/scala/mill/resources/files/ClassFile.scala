// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.resources.files

import java.io.PrintWriter

import mill.controller.AppController
import mill.resources.{Resource, ResourceHandler}
import org.apache.commons.io.FilenameUtils

object ClassFile {

  class ClassFileResourceHandler extends ResourceHandler {
    override def getGroupName = "Java Files"

    override def getGroupResourceName = "Class File"

    override def getDefaultResourceName = "NewFile.java"

    override def getFileResourceExtension = "java"

    override def handleCreate(args: Any*): Resource = null

    override def handleOpen(args: Any*): Resource = {
      val filePath = args(0).asInstanceOf[String]
      val fileName = FilenameUtils.getName(filePath)
      val classFile = new ClassFile(fileName, filePath)

      AppController.instance().openResourceInEditor(fileName, filePath, classFile)

      classFile
    }

    override def isProjectHandler = false
  }

}

class ClassFile(name: String) extends Resource(name) {
  private var parent: PackageFile = _
  private var content: String = _
  private var fullPath: String = _

  def this(name: String, packageFileParent: PackageFile) {
    this(name)
    this.parent = packageFileParent
  }

  def this(name: String, fullPath: String) {
    this(name)

    this.parent = null
    this.fullPath = fullPath
  }

  override def getFullPath: String = if (parent != null) parent.getFullPath + name
  else fullPath

  override def getContent: String = content

  override def setContent(content: String): Unit = {
    this.content = content
  }

  override def saveToFile(path: String): Unit = {
    new PrintWriter(getFullPath) {
      write(content); close()
    }
  }

  def getParent: PackageFile = parent
}