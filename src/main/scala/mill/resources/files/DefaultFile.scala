// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.resources.files

import java.io.{File, IOException}

import mill.controller.AppController
import mill.model.ProjectsRepository
import mill.resources.{Resource, ResourceHandler}
import org.apache.commons.io.{FileUtils, FilenameUtils}

object DefaultFile {

  class DefaultFileResourceHandler extends ResourceHandler {
    override def getGroupName: String = null

    override def getGroupResourceName: String = null

    override def getDefaultResourceName = "NewFile.txt"

    override def getFileResourceExtension = "*.*"

    override def handleCreate(args: Any*): Resource = null

    override def handleOpen(args: Any*): Resource = {
      val filePath = args(0).asInstanceOf[String]
      val fileName = FilenameUtils.getName(filePath)
      val defaultFile = new DefaultFile(fileName, filePath)

      AppController.instance().openResourceInEditor(fileName, filePath, defaultFile)
      ProjectsRepository.instance().addOpenedFile(filePath, defaultFile)

      defaultFile
    }

    override def isProjectHandler = false
  }

}

class DefaultFile(name: String, var fullPath: String) extends Resource(name) {
  private var content: String = _

  override def getFullPath: String = fullPath

  override def getContent: String = content

  override def setContent(content: String): Unit = {
    this.content = content
  }

  override def saveToFile(path: String): Unit = {
    val file = new File(getFullPath)
    try
      FileUtils.writeStringToFile(file, content)
    catch {
      case e: IOException =>
        e.printStackTrace()
    }
  }
}