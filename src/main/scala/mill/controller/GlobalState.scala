// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.controller

import javafx.collections.{FXCollections, ObservableList}
import org.apache.commons.io.FilenameUtils

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
  private val openedFiles: ObservableList[String] = FXCollections.observableArrayList[String]
  private val recentlyOpenedFiles: ObservableList[String] = FXCollections.observableArrayList[String]
  private val recentFiles: ObservableList[String] = FXCollections.observableArrayList[String]

  private var _instance: GlobalState = _

  def instance(): GlobalState = {
    if (_instance == null) _instance = new GlobalState

    _instance
  }

  def getOpenedFiles: ObservableList[String] = openedFiles

  def getRecentlyOpenedFiles: ObservableList[String] = recentlyOpenedFiles

  def addRecentFile(filePath: String): Unit = {
    val normalFilePath = FilenameUtils.normalize(filePath)

    if (!recentFiles.contains(normalFilePath)) recentFiles.add(normalFilePath)
  }

  def getRecentFiles: ObservableList[String] = recentFiles
}
