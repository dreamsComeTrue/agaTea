package mill.ui.editor

import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.control.{Button, SplitPane, ToggleButton, Tooltip}
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.scene.layout.{BorderPane, Pane, StackPane, VBox}
import mill.{Resources, Utilities}
import mill.controller.{AppController, FXStageInitializer, GlobalState}
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.collections.ObservableBuffer.{Add, Change, Remove}

/**
  * Created by Dominik 'squall' Jasiński on 2018-08-17.
  */
object EditorWindow {
  private val SELECT_BORDER_STYLE = "-fx-border-width: 1; -fx-border-color: rgb(130, 130, 130);"
  private val DESELECT_BORDER_STYLE = "-fx-border-width: 0;"
}

class EditorWindow() extends BorderPane with FXStageInitializer {
  private val buffers = new ObservableBuffer[EditorBuffer]()
  private val bufferHeaders = new ObservableBuffer[EditorBufferHeader]()
  private val activeBuffer = new ObjectProperty[EditorBuffer]
  private var activeBufferHeader: EditorBufferHeader = _
  private var ownSplitPane: SplitPane = _
  private var editorToolbox: VBox = _
  private val topContent: Pane = new Pane
  private val centerSplitPane: StackPane = new StackPane

  init()

  private def init(): Unit = {
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
    buffers.onChange((_: ObservableBuffer[EditorBuffer], c: Seq[Change[EditorBuffer]]) => {
      val item: Change[EditorBuffer] = c.head

      item match {
        case Add(_, added) =>
          //  Added buffers needs to be added to the pane
          for (buffer <- added) {
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

            headerButton.assignDuringDrag(() => EditorWindow.this.duringDrag())
            headerButton.assignAfterDrag(() => EditorWindow.this.afterDrag())
            moveHeaderToTheRightOfHeaders(headerButton)

            bufferHeaders.add(headerButton)
            centerSplitPane.getChildren.add(buffer)

            setActiveBuffer(buffer)
          }

        case Remove(_, removed) =>
          def findBufferHeader(buffer: EditorBuffer): EditorBufferHeader = {
            for (bufferHeader1 <- bufferHeaders) {
              if (bufferHeader1.getBuffer != buffer) return bufferHeader1
            }

            null
          }

          for (buffer <- removed) {
            setActiveBuffer(buffer)
            removeBuffer(buffer)

            bufferHeaders.remove(findBufferHeader(buffer))
          }
      }
    }
    )
  }

  private def moveHeaderToTheRightOfHeaders(headerButton: EditorBufferHeader): Unit = {
    if (bufferHeaders.nonEmpty) {
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
    val sorted: ObservableBuffer[EditorBufferHeader] = bufferHeaders.sortWith((o1: EditorBufferHeader, o2: EditorBufferHeader) => {
      java.lang.Double.compare(o1.getContent.getTranslateX, o2.getContent.getTranslateX) > 0
    })

    if (activeBufferHeader != null) {
      val activeContent: Node = activeBufferHeader.getContent
      val activeX: Double = activeContent.getTranslateX
      val activeWidth: Double = activeContent.getLayoutBounds.getWidth
      var activeIndex: Int = -1

      for (i <- sorted.indices) {
        if (sorted(i) == activeBufferHeader) {
          activeIndex = i
        }
      }

      if (activeIndex > 0) {
        val actualContent: Node = sorted(activeIndex - 1).getContent
        val actualX: Double = actualContent.getTranslateX
        val actualWidth: Double = actualContent.getLayoutBounds.getWidth

        if (activeWidth > actualWidth) {
          if (activeX < actualX + actualWidth * 0.5) {
            val tmp: EditorBufferHeader = sorted(activeIndex - 1)
            sorted(activeIndex - 1) = activeBufferHeader
            sorted(activeIndex) = tmp
          }
        }
        else {
          val ratio: Double = activeWidth / actualWidth / 2

          if (activeX < actualX + actualWidth * ratio) {
            val tmp: EditorBufferHeader = sorted(activeIndex - 1)
            sorted(activeIndex - 1) = activeBufferHeader
            sorted(activeIndex) = tmp
          }
        }
      }
      if (activeIndex > -1 && activeIndex < sorted.length - 1) {
        val actualContent: Node = sorted(activeIndex + 1).getContent
        val actualX: Double = actualContent.getTranslateX
        val actualWidth: Double = actualContent.getLayoutBounds.getWidth

        if (activeWidth > actualWidth) {
          if (activeX + activeWidth * 0.5 > actualX) {
            val tmp: EditorBufferHeader = sorted(activeIndex + 1)
            sorted(activeIndex + 1) = activeBufferHeader
            sorted(activeIndex) = tmp
          }
        }
        else {
          val ratio: Double = activeWidth / actualWidth / 2

          if (activeX + activeWidth * ratio > actualX) {
            val tmp: EditorBufferHeader = sorted(activeIndex + 1)
            sorted(activeIndex + 1) = activeBufferHeader
            sorted(activeIndex) = tmp
          }
        }
      }
    }

    var lastSize = 0.0

    for (buffer <- sorted) {
      val content: Node = buffer.getContent

      if (allHeaders || (activeBufferHeader != null && (buffer != activeBufferHeader))) {
        content.setTranslateX(lastSize)
      }

      lastSize += content.getLayoutBounds.getWidth
    }
  }

  private def attachActiveBufferListener(): Unit = {
    activeBuffer.onChange((_, _: EditorBuffer, newValue: EditorBuffer) => {
      bufferHeaders.foreach((header: EditorBufferHeader) => header.setSelected(false))
      bufferHeaders.filter((bufferHeader: EditorBufferHeader) => bufferHeader.getBuffer == newValue).foreach((bufferHeader: EditorBufferHeader) => {
        bufferHeader.setSelected(true)
        bufferHeader.getBuffer.toFront()
      })

      if (newValue != null) {
        newValue.requestFocus()
      }
    }
    )
  }

  private def attachBufferHeadersListener(): Unit = {
    bufferHeaders.onChange((_: ObservableBuffer[EditorBufferHeader], c: Seq[Change[EditorBufferHeader]]) => {
      val item: Change[EditorBufferHeader] = c.head

      item match {
        case Add(_, added) =>
          for (buffer <- added) {
            topContent.getChildren.add(buffer.getContent)
          }
        case Remove(_, removed) =>
          for (buffer <- removed) {
            topContent.getChildren.remove(buffer.getContent)
          }
      }

      AppController.instance().addFXStageInitializer(EditorWindow.this)
    })
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
  def removeBuffer(buffer: EditorBuffer): Unit = {
    //	If this is an active buffer, set 'active flag' to previous one
    var index: Int = -1
    val removeThisBuffer: Boolean = activeBuffer() == buffer

    if (buffer == activeBuffer()) {
      if (buffers.nonEmpty) {
        index = Math.max(0, buffers.indexOf(buffer) - 1)
      }
    }

    buffers.remove(buffer)

    GlobalState.instance().removeOpenedFile(buffer.getPath)

    if (removeThisBuffer) {
      if ((index > -1) && buffers.nonEmpty) {
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
    activeBuffer() = buffer
  }

  /**
    * Returns active buffer for this window
    *
    * @return active buffer
    */
  def getActiveBuffer: EditorBuffer = {
    activeBuffer()
  }

  def moveFocusToActiveBuffer(): Unit = {
    if (activeBuffer() != null) {
      for (buffer <- buffers) {
        buffer.getTextEditor.setCaretVisible(false)
      }

      activeBuffer().getTextEditor.requestFocus()
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
