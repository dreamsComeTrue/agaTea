package mill.ui.editor

import javafx.geometry.Orientation
import javafx.scene.control.SplitPane

import scala.collection.mutable

/**
  * Created by Dominik 'squall' Jasiński on 2018-08-17.
  */
class EditorWindowContainer() extends SplitPane {
  private val paneParentMap = mutable.Map[SplitPane, SplitPane]()

  init()

  private def init(): Unit = {
    this.setOrientation(Orientation.HORIZONTAL)
    this.setMinHeight(0.0)
    this.setMinWidth(0.0)
    this.setStyle("-fx-padding: 0; -fx-background-insets: 0;")
  }

  private def generateSplitContainer(window: EditorWindow, parentSplitPane: SplitPane): SplitPane = {
    val ownSplitPane: SplitPane = new SplitPane
    ownSplitPane.setMinHeight(0.0)
    ownSplitPane.setMinWidth(0.0)
    ownSplitPane.getItems.add(window)
    ownSplitPane.setStyle("-fx-padding: 0; -fx-background-insets: 0;")

    window.setOwnSplitPane(ownSplitPane)

    paneParentMap.put(ownSplitPane, parentSplitPane)

    ownSplitPane
  }

  def addWindow(window: EditorWindow): Unit = {
    val splitContainer: SplitPane = generateSplitContainer(window, this)

    this.getItems.add(splitContainer)
  }

  def removeWindow(window: EditorWindow): Unit = {
    val parent: SplitPane = paneParentMap(window.getOwnSplitPane)
    paneParentMap.remove(window.getOwnSplitPane)

    parent.getItems.remove(window.getOwnSplitPane)

    if (parent.getItems.size == 0) {
      val preParent: SplitPane = paneParentMap(parent)

      if (preParent != null) {
        preParent.getItems.remove(parent)
        paneParentMap.remove(parent)
      }
    }

    if (paneParentMap.size == 1) {
      paneParentMap.clear()
      this.getItems.clear()
    }
  }

  def splitToLeft(window: EditorWindow, oldWindow: EditorWindow): Unit = {
    splitHorizontal(window, oldWindow, true)
  }

  def splitToRight(window: EditorWindow, oldWindow: EditorWindow): Unit = {
    splitHorizontal(window, oldWindow, false)
  }

  def splitToTop(window: EditorWindow, oldWindow: EditorWindow): Unit = {
    splitVertical(window, oldWindow, true)
  }

  def splitToBottom(window: EditorWindow, oldWindow: EditorWindow): Unit = {
    splitVertical(window, oldWindow, false)
  }

  private def splitHorizontal(window: EditorWindow, oldWindow: EditorWindow, toLeft: Boolean): Unit = {
    val oldWindowSplitPane: SplitPane = oldWindow.getOwnSplitPane
    val oldPane: SplitPane = generateSplitContainer(oldWindow, oldWindowSplitPane)
    val newPane: SplitPane = generateSplitContainer(window, oldWindowSplitPane)

    oldWindowSplitPane.getItems.clear()
    oldWindowSplitPane.setOrientation(Orientation.HORIZONTAL)

    if (toLeft) oldWindowSplitPane.getItems.addAll(newPane, oldPane)
    else oldWindowSplitPane.getItems.addAll(oldPane, newPane)

    val size = oldWindowSplitPane.getItems.size

    for (i <- 0 until size - 1) {
      oldWindowSplitPane.setDividerPosition(i, (i.toDouble + 1) / size)
    }
  }

  private def splitVertical(window: EditorWindow, oldWindow: EditorWindow, toTop: Boolean): Unit = {
    val oldWindowSplitPane: SplitPane = oldWindow.getOwnSplitPane
    val oldPane: SplitPane = generateSplitContainer(oldWindow, oldWindowSplitPane)
    val newPane: SplitPane = generateSplitContainer(window, oldWindowSplitPane)

    oldWindowSplitPane.getItems.clear()
    oldWindowSplitPane.setOrientation(Orientation.VERTICAL)

    if (toTop) oldWindowSplitPane.getItems.addAll(newPane, oldPane)
    else oldWindowSplitPane.getItems.addAll(oldPane, newPane)

    val size = oldWindowSplitPane.getItems.size

    for (i <- 0 until size - 1) {
      oldWindowSplitPane.setDividerPosition(i, (i.toDouble + 1) / size)
    }
  }
}
