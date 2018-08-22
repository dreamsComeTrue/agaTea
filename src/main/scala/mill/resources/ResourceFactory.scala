// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.resources

import java.io.File
import java.lang.reflect.{InvocationTargetException, Method}
import java.nio.file.{Files, LinkOption, Path, Paths}

import mill.controller.{AppController, GlobalState}
import mill.model.ProjectsRepository
import mill.resources.files.{ClassFile, DefaultFile, PackageFile}
import mill.{Log, Resources, Utilities}
import org.apache.commons.io.FilenameUtils

import scala.collection.mutable.ListBuffer

object ResourceFactory {
  private var resourceHandlers = ListBuffer[ResourceHandler]()
  private var defaultResourceHandler: ResourceHandler = _

  init()

  private def init(): Unit = {
    new ClassFile.ClassFileResourceHandler
    new PackageFile.PackageFileResourceHandler

    val allClasses: List[Class[_]] = Utilities.getAllResourceClasses

    for (c <- allClasses) {
      val className: String = c.getName

      //	We are adding this to the end of resource handlers
      //	so it is looked as last one from all Resource Handlers
      if (className != "DefaultFile") {
        addResourceHandler(className)
      }
    }

    defaultResourceHandler = new DefaultFile.DefaultFileResourceHandler
  }

  private def addResourceHandler (className: String): Unit ={
    try {
      val clazz = Class.forName(className).asInstanceOf[Class[AnyRef]]

      val o: Any = clazz.newInstance
      val resGroupName: String = "getGroupName"
      var m: Method = clazz.getDeclaredMethod(resGroupName)
      m.invoke(o)

      val resMethodName: String = "getGroupResourceName"
      m = clazz.getDeclaredMethod(resMethodName)
      m.invoke(o)

      resourceHandlers += o.asInstanceOf[ResourceHandler]
    } catch {
      case e@(_: IllegalAccessException | _: NoSuchMethodException | _: InvocationTargetException | _: InstantiationException | _: ClassNotFoundException) =>
        e.printStackTrace()
    }
  }

  def getResourceHandlers: ListBuffer[ResourceHandler] = resourceHandlers

  def handleFileOpen(fileName: String): Resource = {
    var res: Resource = null
    val path: Path = Paths.get(fileName)

    if (Files.exists(path) && !Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
      val file: String = FilenameUtils.getName(fileName)
      val filePath: String = FilenameUtils.normalize(FilenameUtils.getFullPath(fileName) + File.separatorChar + file)

      //  If file is already loaded...
      if (ProjectsRepository.instance().isFileOpened(filePath)) {
        //  ...and it's in EditorWindow - then focus it
        if (AppController.instance().focusEditor(filePath)) return ProjectsRepository.instance().getOpenedFile(filePath)
      }

      if (ProjectsRepository.instance().isProjectOpened(filePath)) return ProjectsRepository.instance().getOpenedProject(filePath)

      var foundHandler: Boolean = false

      //  Try to open resource with registered handlers
      for (handler <- resourceHandlers) {
        if ((handler.getFileResourceExtension != null) && file.endsWith(handler.getFileResourceExtension)) {
          res = handler.handleOpen(filePath)
          foundHandler = true
        }
      }

      if (!foundHandler) res = defaultResourceHandler.handleOpen(filePath, false)
      if (!res.isInstanceOf[Project]) GlobalState.instance().addOpenedFile(filePath)
    }
    else Log.info(String.format(Resources.FILE_NOT_EXIST, fileName) + System.lineSeparator)

    res
  }
}
