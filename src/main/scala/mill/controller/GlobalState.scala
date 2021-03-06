// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.controller

import org.apache.commons.io.FilenameUtils
import scalafx.collections.ObservableBuffer

class GlobalState private() {
  def isFileOpened(filePath: String): Boolean = {
    val normalFilePath = FilenameUtils.normalize(filePath)
    GlobalState.openedFiles.indexOf(normalFilePath) >= 0
  }

  def addOpenedFile(filePath: String): Unit = {
    val normalFilePath = FilenameUtils.normalize(filePath)
    GlobalState.addRecentFile(normalFilePath)

    if (!isFileOpened(normalFilePath)) {

      GlobalState.openedFiles.add(normalFilePath)

      if (GlobalState.recentlyOpenedFiles.contains(normalFilePath)) GlobalState.recentlyOpenedFiles.remove(normalFilePath)
    }
  }

  def removeOpenedFile(filePath: String): Unit = {
    val normalFilePath = FilenameUtils.normalize(filePath)

    GlobalState.openedFiles.remove(normalFilePath)

    if (!GlobalState.recentlyOpenedFiles.contains(normalFilePath)) GlobalState.recentlyOpenedFiles.add(normalFilePath)
  }
}

object GlobalState {
  private val openedFiles = new ObservableBuffer[String]()
  private val recentlyOpenedFiles = new ObservableBuffer[String]()
  private val recentFiles = new ObservableBuffer[String]()

  private var _instance: GlobalState = _

  def instance(): GlobalState = {
    if (_instance == null) _instance = new GlobalState

    _instance
  }

  def getOpenedFiles: ObservableBuffer[String] = openedFiles

  def getRecentlyOpenedFiles: ObservableBuffer[String] = recentlyOpenedFiles

  def addRecentFile(filePath: String): Unit = {
    val normalFilePath = FilenameUtils.normalize(filePath)

    if (!recentFiles.contains(normalFilePath)) recentFiles.add(normalFilePath)
  }

  def getRecentFiles: ObservableBuffer[String] = recentFiles
}
