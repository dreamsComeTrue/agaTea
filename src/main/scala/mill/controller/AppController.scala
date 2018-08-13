// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.controller

import java.io.File

import mill.ui.{ContentPane, MainContent, TextEditor}

class AppController private(val mainContent: MainContent) {
  def addTab(header: String, text: String, path: String = ""): Unit = {
    val contentPane = mainContent.centerPane.getChildren.get(0).asInstanceOf[ContentPane]
    contentPane.addTab(header, text, path)
  }

  def addTab(file: File): Unit = {
    val contentPane = mainContent.centerPane.getChildren.get(0).asInstanceOf[ContentPane]
    contentPane.addTab(file)
  }

  def getCurrentTextEditor: TextEditor = {
    val contentPane = mainContent.centerPane.getChildren.get(0).asInstanceOf[ContentPane]
    contentPane.getCurrentTextEditor
  }
}

object AppController {
  private var _instance: AppController = _

  def initialize(mainContent: MainContent): Unit = {
    if (_instance == null) _instance = new AppController(mainContent)
  }

  def instance() = {
    _instance
  }
}
