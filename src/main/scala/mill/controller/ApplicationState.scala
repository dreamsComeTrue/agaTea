// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.controller

import java.util.function.Consumer

import javafx.scene.Node

abstract class ApplicationState {
  private var ownContent : Node = _

  protected var onFirstViewStart: Consumer[Void] = _
  protected var onFirstViewCompleted: Consumer[Void] = _
  protected var onFirstTransitionFinished: Consumer[Void] = _
  protected var onSecondViewCompleted: Consumer[Void] = _

  def process(lastStateContent: Node): Unit

  def setContent(content: Node): Unit = {
    ownContent = content
  }

  def getContent: Node = ownContent

  def getOnFirstViewStart: Consumer[Void] = onFirstViewStart

  def setOnFirstViewStart(onFirstViewStart: Consumer[Void]): Unit = {
    this.onFirstViewStart = onFirstViewStart
  }

  def getOnFirstViewCompleted: Consumer[Void] = onFirstViewCompleted

  def setOnFirstViewCompleted(onFirstViewCompleted: Consumer[Void]): Unit = {
    this.onFirstViewCompleted = onFirstViewCompleted
  }

  def getOnFirstTransitionFinished: Consumer[Void] = onFirstTransitionFinished

  def setOnFirstTransitionFinished(onFirstTransitionFinished: Consumer[Void]): Unit = {
    this.onFirstTransitionFinished = onFirstTransitionFinished
  }

  def getOnSecondViewCompleted: Consumer[Void] = onSecondViewCompleted

  def setOnSecondViewCompleted(onSecondViewCompleted: Consumer[Void]): Unit = {
    this.onSecondViewCompleted = onSecondViewCompleted
  }
}
