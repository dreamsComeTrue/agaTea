package mill.ui.editor

import java.util.Comparator

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.transformation.SortedList
import javafx.collections.{FXCollections, ListChangeListener, ObservableList}
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.{Button, SplitPane, ToggleButton, Tooltip}
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.layout.{BorderPane, Pane, StackPane, VBox}
import mill.controller.{AppController, FXStageInitializer, GlobalState}
import mill.{Resources, Utilities}

import scala.collection.JavaConverters._

/**
  * Created by Dominik 'squall' Jasiński on 2018-08-17.
  */
object EditorWindow {
  private val SELECT_BORDER_STYLE = "-fx-border-width: 1; -fx-border-color: rgb(130, 130, 130);"
  private val DESELECT_BORDER_STYLE = "-fx-border-width: 0;"
}

class EditorWindow() extends BorderPane with FXStageInitializer {
  private val buffers: ObservableList[EditorBuffer] = FXCollections.observableArrayList[EditorBuffer]
  private val bufferHeaders: ObservableList[EditorBufferHeader] = FXCollections.observableArrayList[EditorBufferHeader]
  private val activeBuffer: SimpleObjectProperty[EditorBuffer] = new SimpleObjectProperty[EditorBuffer]
  private var activeBufferHeader: EditorBufferHeader = _
  private var ownSplitPane: SplitPane = _
  private var editorToolbox: VBox = _
  private val topContent: Pane = new Pane
  private val centerSplitPane: StackPane = new StackPane

  init()

  private def init() {
    centerSplitPane.setStyle(EditorWindow.DESELECT_BORDER_STYLE)
    editorToolbox = createEditorToolbox

    this.setTop(topContent)
    this.setLeft(editorToolbox)
    this.setCenter(centerSplitPane)
    this.setMinHeight(0.0)

    attachBufferHeadersListener()
    attachBuffersListener()
    attachActiveBufferListener()
  }

  private def attachBuffersListener(): Unit = {
    buffers.addListener(new ListChangeListener[EditorBuffer] {
      override def onChanged(c: ListChangeListener.Change[_ <: EditorBuffer]): Unit = {
        c.next

        //  Added buffers needs to be added to the pane
        if (c.wasAdded) {
          for (buffer <- c.getAddedSubList.asScala) {
            val headerButton: EditorBufferHeader = new EditorBufferHeader(buffer)
            headerButton.assignOnClick((event: MouseEvent) => {
              activeBufferHeader = headerButton
              //	Middle click on header - close tab
              if (event.getButton == MouseButton.MIDDLE) {
                setActiveBuffer(buffer)
                removeBuffer(buffer)
                bufferHeaders.remove(headerButton)
                event.consume()
              }
              else { //	Un-click all other buttons
                setActiveBuffer(buffer)
                headerButton.getBuffer.toFront()
                buffer.requestFocus()
              }
              if (event.getClickCount == 2) {
                AppController.instance().maximizeEditorBuffer(!AppController.instance().getProjectExplorerVisible)
              }
            })

            headerButton.assignOnClose((event: MouseEvent) => {
              if (!(event.getButton == MouseButton.SECONDARY)) {
                setActiveBuffer(buffer)
                removeBuffer(buffer)
                bufferHeaders.remove(headerButton)
                event.consume()
              }
            })

            headerButton.assignDuringDrag(new Runnable {
              override def run(): Unit = EditorWindow.this.duringDrag()
            })
            headerButton.assignAfterDrag(new Runnable {
              override def run(): Unit = EditorWindow.this.afterDrag()
            })
            moveHeaderToTheRightOfHeaders(headerButton)

            bufferHeaders.add(headerButton)
            centerSplitPane.getChildren.add(buffer)

            setActiveBuffer(buffer)
          }
        }
        else {
          if (c.wasRemoved) {
            for (buffer <- c.getRemoved.asScala) {
              var bufferHeader: EditorBufferHeader = null

              for (bufferHeader1 <- bufferHeaders.asScala) {
                if (bufferHeader1.getBuffer != buffer) {
                  bufferHeader = bufferHeader1
                }
              }

              bufferHeaders.remove(bufferHeader)
              centerSplitPane.getChildren.remove(buffer)
            }
          }
        }
      }
    })
  }

  private def moveHeaderToTheRightOfHeaders(headerButton: EditorBufferHeader): Unit = {
    if (bufferHeaders.size > 0) {
      val lastHeaderContent: Node = bufferHeaders.get(bufferHeaders.size - 1).getContent
      headerButton.getContent.setTranslateX(lastHeaderContent.getTranslateX + lastHeaderContent.getLayoutBounds.getWidth)
    }
  }

  private def duringDrag(): Unit = {
    rearrangeHeaders(false)
  }

  private def afterDrag(): Unit = {
    rearrangeHeaders(true)
  }

  private def rearrangeHeaders(allHeaders: Boolean): Unit = {
    val sorted: SortedList[EditorBufferHeader] = bufferHeaders.sorted(new Comparator[EditorBufferHeader] {
      override def compare(o1: EditorBufferHeader, o2: EditorBufferHeader): Int = {
        java.lang.Double.compare(o1.getContent.getTranslateX, o2.getContent.getTranslateX)
      }
    })

    if (activeBufferHeader != null) {
      val activeContent: Node = activeBufferHeader.getContent
      val activeX: Double = activeContent.getTranslateX
      val activeWidth: Double = activeContent.getLayoutBounds.getWidth
      var activeIndex: Int = -1

      for (i <- 0 until sorted.size()) {
        if (sorted.get(i) == activeBufferHeader) {
          activeIndex = i
        }
      }

      if (activeIndex > 0) {
        val actualContent: Node = sorted.get(activeIndex - 1).getContent
        val actualX: Double = actualContent.getTranslateX
        val actualWidth: Double = actualContent.getLayoutBounds.getWidth

        if (activeWidth > actualWidth) {
          if (activeX < actualX + actualWidth * 0.5) {
            val tmp: EditorBufferHeader = sorted.get(activeIndex - 1)
            sorted.set(activeIndex - 1, activeBufferHeader)
            sorted.set(activeIndex, tmp)
          }
        }
        else {
          val ratio: Double = activeWidth / actualWidth / 2
          if (activeX < actualX + actualWidth * ratio) {
            val tmp: EditorBufferHeader = sorted.get(activeIndex - 1)
            sorted.set(activeIndex - 1, activeBufferHeader)
            sorted.set(activeIndex, tmp)
          }
        }
      }
      if (activeIndex > -1 && activeIndex < sorted.size() - 1) {
        val actualContent: Node = sorted.get(activeIndex + 1).getContent
        val actualX: Double = actualContent.getTranslateX
        val actualWidth: Double = actualContent.getLayoutBounds.getWidth

        if (activeWidth > actualWidth) {
          if (activeX + activeWidth * 0.5 > actualX) {
            val tmp: EditorBufferHeader = sorted.get(activeIndex + 1)
            sorted.set(activeIndex + 1, activeBufferHeader)
            sorted.set(activeIndex, tmp)
          }
        }
        else {
          val ratio: Double = activeWidth / actualWidth / 2

          if (activeX + activeWidth * ratio > actualX) {
            val tmp: EditorBufferHeader = sorted.get(activeIndex + 1)
            sorted.set(activeIndex + 1, activeBufferHeader)
            sorted.set(activeIndex, tmp)
          }
        }
      }
    }

    var lastSize = 0.0

    for (buffer <- sorted.asScala) {
      val content: Node = buffer.getContent

      if (allHeaders || (activeBufferHeader != null && (buffer != activeBufferHeader))) {
        content.setTranslateX(lastSize)
      }

      lastSize += content.getLayoutBounds.getWidth
    }
  }

  private def attachActiveBufferListener(): Unit = {
    activeBuffer.addListener(new ChangeListener[EditorBuffer] {
      override def changed(observable: ObservableValue[_ <: EditorBuffer], oldValue: EditorBuffer, newValue: EditorBuffer): Unit = {
        bufferHeaders.stream.forEach((header: EditorBufferHeader) => header.setSelected(false))
        bufferHeaders.stream.filter((bufferHeader: EditorBufferHeader) => bufferHeader.getBuffer == newValue).forEach((bufferHeader: EditorBufferHeader) => {
          bufferHeader.setSelected(true)
          bufferHeader.getBuffer.toFront()
        })

        if (newValue != null) {
          newValue.requestFocus()
        }
      }
    })
  }

  private def attachBufferHeadersListener(): Unit = {
    bufferHeaders.addListener(new ListChangeListener[EditorBufferHeader] {
      override def onChanged(c: ListChangeListener.Change[_ <: EditorBufferHeader]): Unit = {
        c.next
        if (c.wasAdded) {
          for (buffer <- c.getAddedSubList.asScala) {
            topContent.getChildren.add(buffer.getContent)
          }
        }
        else {
          if (c.wasRemoved) {
            for (buffer <- c.getRemoved.asScala) {
              topContent.getChildren.remove(buffer.getContent)
            }
          }
        }

        AppController.instance().addFXStageInitializer(EditorWindow.this)
      }
    }.asInstanceOf[ListChangeListener[EditorBufferHeader]])
  }

  override def fxInitialize: Boolean = {
    duringDrag()

    true
  }

  /**
    * Creates side bar with buttons for source editor
    *
    * @return pane with buttons for source editor events handling
    */
  private def createEditorToolbox: VBox = {
    editorToolbox = new VBox
    editorToolbox.setSpacing(0.5)

    val cutButton: Button = Utilities.createButton(Resources.Images.IMAGE_CUT, 20, Utilities.DEFAULT_IMAGE_PADDING)
    cutButton.setTooltip(new Tooltip(Resources.CUT))
    val copyButton: Button = Utilities.createButton(Resources.Images.IMAGE_COPY, 20, Utilities.DEFAULT_IMAGE_PADDING)
    copyButton.setTooltip(new Tooltip(Resources.COPY))
    val pasteButton: Button = Utilities.createButton(Resources.Images.IMAGE_PASTE, 20, Utilities.DEFAULT_IMAGE_PADDING)
    pasteButton.setTooltip(new Tooltip(Resources.PASTE))
    val undoButton: Button = Utilities.createButton(Resources.Images.IMAGE_UNDO, 20, Utilities.DEFAULT_IMAGE_PADDING)
    undoButton.setTooltip(new Tooltip(Resources.UNDO))
    val redoButton: Button = Utilities.createButton(Resources.Images.IMAGE_REDO, 20, Utilities.DEFAULT_IMAGE_PADDING)
    redoButton.setTooltip(new Tooltip(Resources.REDO))
    val bookmarkButton: Button = Utilities.createButton(Resources.Images.IMAGE_BOOKMARK, 20, Utilities.DEFAULT_IMAGE_PADDING)
    bookmarkButton.setTooltip(new Tooltip(Resources.ADD_BOOKMARK))
    val bookmarksButton: Button = Utilities.createButton(Resources.Images.IMAGE_BOOKMARKS, 20, Utilities.DEFAULT_IMAGE_PADDING)
    bookmarksButton.setTooltip(new Tooltip(Resources.SHOW_ALL_BOOKMARKS))
    val previousBookmarkButton: Button = Utilities.createButton(Resources.Images.IMAGE_PREVIOUS, 20, Utilities.DEFAULT_IMAGE_PADDING)
    previousBookmarkButton.setTooltip(new Tooltip(Resources.PREVIOUS_BOOKMARK))
    val nextBookmarkButton: Button = Utilities.createButton(Resources.Images.IMAGE_NEXT, 20, Utilities.DEFAULT_IMAGE_PADDING)
    nextBookmarkButton.setTooltip(new Tooltip(Resources.NEXT_BOOKMARK))
    val searchButton: Button = Utilities.createButton(Resources.Images.IMAGE_SEARCH, 20, Utilities.DEFAULT_IMAGE_PADDING)
    searchButton.setTooltip(new Tooltip(Resources.SEARCH))
    val formatButton: Button = Utilities.createButton(Resources.Images.IMAGE_FORMAT, 20, Utilities.DEFAULT_IMAGE_PADDING)
    formatButton.setTooltip(new Tooltip(Resources.FORMAT_CODE))
    val wordWrapButton: ToggleButton = Utilities.createToggleButton(Resources.Images.IMAGE_WRAP, 20, Utilities.DEFAULT_IMAGE_PADDING)
    wordWrapButton.setTooltip(new Tooltip(Resources.LINE_WRAP))

    VBox.setMargin(undoButton, new Insets(10, 0, 0, 0))
    VBox.setMargin(bookmarkButton, new Insets(10, 0, 0, 0))
    VBox.setMargin(searchButton, new Insets(10, 0, 0, 0))

    searchButton.setOnAction((_: ActionEvent) => {
    })

    editorToolbox.getChildren.addAll(cutButton, copyButton, pasteButton, undoButton, redoButton, bookmarkButton, bookmarksButton, previousBookmarkButton, nextBookmarkButton, searchButton, formatButton, wordWrapButton)
    editorToolbox.setOnMouseClicked((_: MouseEvent) => AppController.instance().setActiveEditorWindow(EditorWindow.this))

    editorToolbox
  }

  def addBuffer(buffer: EditorBuffer): EditorBuffer = {
    buffer.setWindow(this)
    buffers.add(buffer)

    setActiveBuffer(buffer)

    buffer
  }

  def addBuffer(title: String): EditorBuffer = {
    val buffer: EditorBuffer = new EditorBuffer(this, title)
    buffers.add(buffer)

    setActiveBuffer(buffer)

    buffer
  }

  /**
    * Removes given buffer from this window set
    *
    * @param buffer buffer to remove
    */
  def removeBuffer(buffer: EditorBuffer): Unit = { //	If this is an active buffer, set 'active flag' to previous one
    var index: Int = -1
    val removeThisBuffer: Boolean = activeBuffer.get == buffer

    if (buffer == activeBuffer.get) {
      if (buffers.size > 0) {
        index = Math.max(0, buffers.indexOf(buffer) - 1)
      }
    }

    buffers.remove(buffer)

    GlobalState.instance().removeOpenedFile(buffer.getPath)

    if (removeThisBuffer) {
      if ((index > -1) && (buffers.size > 0)) {
        setActiveBuffer(buffers.get(index))
      }
      else {
        setActiveBuffer(null)
      }
    }
  }

  /**
    * Set active buffer for this window
    */
  def setActiveBuffer(buffer: EditorBuffer): Unit = {
    activeBuffer.set(buffer)
  }

  /**
    * Returns active buffer for this window
    *
    * @return active buffer
    */
  def getActiveBuffer: EditorBuffer = {
    activeBuffer.get
  }

  def moveFocusToActiveBuffer(): Unit = {
    if (activeBuffer.get != null) {
      for (buffer <- buffers.asScala) {
        buffer.getTextEditor.setCaretVisible(false)
      }

      activeBuffer.get.getTextEditor.requestFocus()
    }
  }

  def getBuffers: ObservableList[EditorBuffer] = {
    buffers
  }

  def setOwnSplitPane(ownSplitPane: SplitPane): Unit = {
    this.ownSplitPane = ownSplitPane
  }

  def getOwnSplitPane: SplitPane = {
    ownSplitPane
  }

  def setActive(isActive: Boolean): Unit = {
    if (isActive) {
      centerSplitPane.setStyle(EditorWindow.SELECT_BORDER_STYLE)
    }
    else {
      centerSplitPane.setStyle(EditorWindow.DESELECT_BORDER_STYLE)
    }
  }
}
