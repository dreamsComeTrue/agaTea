// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.input.{KeyCode, KeyEvent}
import mill.Resources
import mill.model.ActionShortcut.ActionType

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ActionShortcut private() {
  private var actionType: ActionType.ActionType = _
  private var binding: List[KeyCode] = _
  private var description: String = _
  private var keyCombination: String = _

  def this(actionType: ActionType.ActionType, description: String, bindingKeys: KeyCode*) {
    this()

    this.actionType = actionType
    this.description = description
    this.binding = List(bindingKeys: _*)

    updateKeyCombination()
  }


  def updateKeyCombination(): Unit = {
    this.keyCombination = ""

    for (i <- binding.indices) {
      this.keyCombination += binding(i).getName

      if (i < binding.size - 1) this.keyCombination += " + "
    }
  }

  def getActionType: ActionType.ActionType = actionType

  def getBinding: List[KeyCode] = binding

  def getKeyCombination: String = keyCombination

  def getDescription: String = description
}

object ActionShortcut {
  object ActionType extends Enumeration {
    type ActionType = Value
    val MOVE_CURSOR_FORWARD, MOVE_CURSOR_BACKWARD, MOVE_CURSOR_UP, MOVE_CURSOR_DOWN, SELECT_PREVIOUS_WORD, SELECT_NEXT_WORD, MOVE_CURSOR_LINE_BEGIN, MOVE_CURSOR_LINE_END, MOVE_CURSOR_PAGE_UP, MOVE_CURSOR_PAGE_DOWN, MOVE_CURSOR_FILE_BEGIN, MOVE_CURSOR_FILE_END, DELETE_FORWARD, DELETE_BACKWARD, NEW_LINE, INSERT_TAB, UNDO, REDO, CUT, COPY, PASTE, SELECT_ALL, GOTO_LINE, NEW_FILE, OPEN_FILE, SAVE_FILE, SELECT_PREVIOUS_BUFFER, SELECT_NEXT_BUFFER, CLOSE_DOCUMENT, SWITCH_TO_NEXT_DOCUMENT, SWITCH_TO_PREVIOUS_DOCUMENT, SELECT_NEXT_TOOL_WINDOW, SELECT_PREVIOUS_TOOL_WINDOW, QUICK_ACCESS, BUILD_PROJECT, RUN_PROJECT, OPEN_SETTINGS, PRODUCTIVE_MODE = Value
  }

  var actions: mutable.Map[ActionType.ActionType, ActionShortcut] = mutable.Map[ActionType.ActionType, ActionShortcut]()
  var actionsList: ListBuffer[ActionShortcut] = ListBuffer[ActionShortcut]()
  private val actionsDirty = new SimpleBooleanProperty(false)

  private var _instance: ActionShortcut = _

  def instance(): ActionShortcut = {
    if (_instance == null) _instance = new ActionShortcut

    _instance
  }

  def checkKeyCombination(actionType: ActionType.ActionType, keyEvent: KeyEvent): Boolean = {
    val altKey = if (keyEvent.isAltDown && (keyEvent.getCode ne KeyCode.ALT)) KeyCode.ALT
    else null

    val controlKey = if (keyEvent.isControlDown && (keyEvent.getCode ne KeyCode.CONTROL)) KeyCode.CONTROL
    else null

    val shiftKey = if (keyEvent.isShiftDown && (keyEvent.getCode ne KeyCode.SHIFT)) KeyCode.SHIFT
    else null

    checkKeyCombination(actionType, keyEvent.getCode, altKey, controlKey, shiftKey)
  }

  private def checkKeyCombination(actionType: ActionType.ActionType, pressedKeys: KeyCode*) = {
    val keys: List[KeyCode] = actions(actionType).getBinding
    var matchedKeys = 0
    var howManyKeys = 0

    for (keyCode <- pressedKeys) {
      if (keyCode != null) {
        howManyKeys += 1

        if (keys.contains(keyCode)) matchedKeys += 1
      }
    }
    (matchedKeys == howManyKeys) && (matchedKeys == keys.size)
  }

  def getActionsDirty: Boolean = actionsDirty.get

  def actionsDirtyProperty: SimpleBooleanProperty = actionsDirty

  def setActionsDirty(actionsDirty: Boolean): Unit = {
    this.actionsDirty.set(actionsDirty)
  }

  val ACTION_MOVE_CURSOR_FORWARD = new ActionShortcut(ActionType.MOVE_CURSOR_FORWARD, Resources.MOVE_CURSOR_FORWARD, KeyCode.RIGHT)
  val ACTION_MOVE_CURSOR_BACKWARD = new ActionShortcut(ActionType.MOVE_CURSOR_BACKWARD, Resources.MOVE_CURSOR_BACKWARD, KeyCode.LEFT)
  val ACTION_MOVE_CURSOR_UP = new ActionShortcut(ActionType.MOVE_CURSOR_UP, Resources.MOVE_CURSOR_UP, KeyCode.UP)
  val ACTION_MOVE_CURSOR_DOWN = new ActionShortcut(ActionType.MOVE_CURSOR_DOWN, Resources.MOVE_CURSOR_DOWN, KeyCode.DOWN)
  val ACTION_SELECT_PREVIOUS_WORD = new ActionShortcut(ActionType.SELECT_PREVIOUS_WORD, Resources.SELECT_PREVIOUS_WORD, KeyCode.CONTROL, KeyCode.LEFT)
  val ACTION_SELECT_NEXT_WORD = new ActionShortcut(ActionType.SELECT_NEXT_WORD, Resources.SELECT_NEXT_WORD, KeyCode.CONTROL, KeyCode.RIGHT)
  val ACTION_MOVE_CURSOR_LINE_BEGIN = new ActionShortcut(ActionType.MOVE_CURSOR_LINE_BEGIN, Resources.BEGIN_OF_LINE, KeyCode.HOME)
  val ACTION_MOVE_CURSOR_LINE_END = new ActionShortcut(ActionType.MOVE_CURSOR_LINE_END, Resources.END_OF_LINE, KeyCode.END)
  val ACTION_MOVE_CURSOR_PAGE_UP = new ActionShortcut(ActionType.MOVE_CURSOR_PAGE_UP, Resources.MOVE_CURSOR_PAGE_UP, KeyCode.PAGE_UP)
  val ACTION_MOVE_CURSOR_PAGE_DOWN = new ActionShortcut(ActionType.MOVE_CURSOR_PAGE_DOWN, Resources.MOVE_CURSOR_PAGE_DOWN, KeyCode.PAGE_DOWN)
  val ACTION_MOVE_CURSOR_FILE_BEGIN = new ActionShortcut(ActionType.MOVE_CURSOR_FILE_BEGIN, Resources.BEGIN_OF_FILE, KeyCode.CONTROL, KeyCode.HOME)
  val ACTION_MOVE_CURSOR_FILE_END = new ActionShortcut(ActionType.MOVE_CURSOR_FILE_END, Resources.END_OF_FILE, KeyCode.CONTROL, KeyCode.END)
  val ACTION_DELETE_FORWARD = new ActionShortcut(ActionType.DELETE_FORWARD, Resources.DELETE_FORWARD, KeyCode.DELETE)
  val ACTION_DELETE_BACKWARD = new ActionShortcut(ActionType.DELETE_BACKWARD, Resources.DELETE_BACKWARD, KeyCode.BACK_SPACE)
  val ACTION_NEW_LINE = new ActionShortcut(ActionType.NEW_LINE, Resources.NEW_LINE, KeyCode.ENTER)
  val ACTION_INSERT_TAB = new ActionShortcut(ActionType.INSERT_TAB, Resources.INSERT_TAB, KeyCode.TAB)
  val ACTION_UNDO = new ActionShortcut(ActionType.UNDO, Resources.UNDO, KeyCode.CONTROL, KeyCode.Z)
  val ACTION_REDO = new ActionShortcut(ActionType.REDO, Resources.REDO, KeyCode.CONTROL, KeyCode.Y)
  val ACTION_CUT = new ActionShortcut(ActionType.CUT, Resources.CUT, KeyCode.CONTROL, KeyCode.X)
  val ACTION_COPY = new ActionShortcut(ActionType.COPY, Resources.COPY, KeyCode.CONTROL, KeyCode.C)
  val ACTION_PASTE = new ActionShortcut(ActionType.PASTE, Resources.PASTE, KeyCode.CONTROL, KeyCode.V)
  val ACTION_SELECT_ALL = new ActionShortcut(ActionType.SELECT_ALL, Resources.SELECT_ALL, KeyCode.CONTROL, KeyCode.A)
  val ACTION_GOTO_LINE = new ActionShortcut(ActionType.GOTO_LINE, Resources.GOTO_LINE_COLUMN, KeyCode.CONTROL, KeyCode.L)
  val ACTION_NEW_FILE = new ActionShortcut(ActionType.NEW_FILE, Resources.NEW_FILE, KeyCode.CONTROL, KeyCode.N)
  val ACTION_OPEN_FILE = new ActionShortcut(ActionType.OPEN_FILE, Resources.OPEN_FILE_OR_PROJECT, KeyCode.CONTROL, KeyCode.O)
  val ACTION_SAVE_FILE = new ActionShortcut(ActionType.SAVE_FILE, Resources.SAVE_FILE, KeyCode.CONTROL, KeyCode.S)
  val ACTION_SWITCH_TO_PREVIOUS_DOCUMENT = new ActionShortcut(ActionType.SWITCH_TO_PREVIOUS_DOCUMENT, Resources.SWITCH_TO_PREVIOUS_FILE, KeyCode.CONTROL, KeyCode.SHIFT, KeyCode.TAB)
  val ACTION_SWITCH_TO_NEXT_DOCUMENT = new ActionShortcut(ActionType.SWITCH_TO_NEXT_DOCUMENT, Resources.SWITCH_TO_NEXT_FILE, KeyCode.CONTROL, KeyCode.TAB)
  val ACTION_CLOSE_DOCUMENT = new ActionShortcut(ActionType.CLOSE_DOCUMENT, Resources.CLOSE_CURRENT_FILE, KeyCode.CONTROL, KeyCode.W)
  val ACTION_SELECT_PREVIOUS_BUFFER = new ActionShortcut(ActionType.SELECT_PREVIOUS_BUFFER, Resources.SELECT_PREVIOUS_BUFFER, KeyCode.CONTROL, KeyCode.ALT, KeyCode.LEFT)
  val ACTION_SELECT_NEXT_BUFFER = new ActionShortcut(ActionType.SELECT_NEXT_BUFFER, Resources.SELECT_NEXT_BUFFER, KeyCode.CONTROL, KeyCode.ALT, KeyCode.RIGHT)
  val ACTION_SELECT_PREVIOUS_TOOL_WINDOW = new ActionShortcut(ActionType.SELECT_PREVIOUS_TOOL_WINDOW, Resources.SELECT_PREVIOUS_TOOL_WINDOW, KeyCode.CONTROL, KeyCode.SHIFT, KeyCode.DIGIT1)
  val ACTION_SELECT_NEXT_TOOL_WINDOW = new ActionShortcut(ActionType.SELECT_NEXT_TOOL_WINDOW, Resources.SELECT_NEXT_TOOL_WINDOW, KeyCode.CONTROL, KeyCode.DIGIT1)
  val ACTION_QUICK_ACCESS = new ActionShortcut(ActionType.QUICK_ACCESS, Resources.QUICK_ACCESS, KeyCode.F1)
  val ACTION_BUILD_PROJECT = new ActionShortcut(ActionType.BUILD_PROJECT, Resources.BUILD_PROJECT, KeyCode.SHIFT, KeyCode.F5)
  val ACTION_RUN_PROJECT = new ActionShortcut(ActionType.RUN_PROJECT, Resources.RUN_PROJECT, KeyCode.F5)
  val ACTION_OPEN_SETTINGS = new ActionShortcut(ActionType.OPEN_SETTINGS, Resources.OPEN_SETTINGS, KeyCode.CONTROL, KeyCode.ALT, KeyCode.S)
  val ACTION_PRODUCTIVE_MODE = new ActionShortcut(ActionType.PRODUCTIVE_MODE, Resources.PRODUCTIVE_MODE, KeyCode.F11)

  actions.put(ActionType.MOVE_CURSOR_FORWARD, ACTION_MOVE_CURSOR_FORWARD)
  actions.put(ActionType.MOVE_CURSOR_BACKWARD, ACTION_MOVE_CURSOR_BACKWARD)
  actions.put(ActionType.MOVE_CURSOR_UP, ACTION_MOVE_CURSOR_UP)
  actions.put(ActionType.MOVE_CURSOR_DOWN, ACTION_MOVE_CURSOR_DOWN)
  actions.put(ActionType.SELECT_PREVIOUS_WORD, ACTION_SELECT_PREVIOUS_WORD)
  actions.put(ActionType.SELECT_NEXT_WORD, ACTION_SELECT_NEXT_WORD)
  actions.put(ActionType.MOVE_CURSOR_LINE_BEGIN, ACTION_MOVE_CURSOR_LINE_BEGIN)
  actions.put(ActionType.MOVE_CURSOR_LINE_END, ACTION_MOVE_CURSOR_LINE_END)
  actions.put(ActionType.MOVE_CURSOR_PAGE_UP, ACTION_MOVE_CURSOR_PAGE_UP)
  actions.put(ActionType.MOVE_CURSOR_PAGE_DOWN, ACTION_MOVE_CURSOR_PAGE_DOWN)
  actions.put(ActionType.MOVE_CURSOR_FILE_BEGIN, ACTION_MOVE_CURSOR_FILE_BEGIN)
  actions.put(ActionType.MOVE_CURSOR_FILE_END, ACTION_MOVE_CURSOR_FILE_END)
  actions.put(ActionType.DELETE_FORWARD, ACTION_DELETE_FORWARD)
  actions.put(ActionType.DELETE_BACKWARD, ACTION_DELETE_BACKWARD)
  actions.put(ActionType.NEW_LINE, ACTION_NEW_LINE)
  actions.put(ActionType.INSERT_TAB, ACTION_INSERT_TAB)
  actions.put(ActionType.UNDO, ACTION_UNDO)
  actions.put(ActionType.REDO, ACTION_REDO)
  actions.put(ActionType.CUT, ACTION_CUT)
  actions.put(ActionType.COPY, ACTION_COPY)
  actions.put(ActionType.PASTE, ACTION_PASTE)
  actions.put(ActionType.SELECT_ALL, ACTION_SELECT_ALL)
  actions.put(ActionType.GOTO_LINE, ACTION_GOTO_LINE)
  actions.put(ActionType.NEW_FILE, ACTION_NEW_FILE)
  actions.put(ActionType.OPEN_FILE, ACTION_OPEN_FILE)
  actions.put(ActionType.SAVE_FILE, ACTION_SAVE_FILE)
  actions.put(ActionType.CLOSE_DOCUMENT, ACTION_CLOSE_DOCUMENT)
  actions.put(ActionType.SELECT_PREVIOUS_BUFFER, ACTION_SELECT_PREVIOUS_BUFFER)
  actions.put(ActionType.SELECT_NEXT_BUFFER, ACTION_SELECT_NEXT_BUFFER)
  actions.put(ActionType.SWITCH_TO_NEXT_DOCUMENT, ACTION_SWITCH_TO_NEXT_DOCUMENT)
  actions.put(ActionType.SWITCH_TO_PREVIOUS_DOCUMENT, ACTION_SWITCH_TO_PREVIOUS_DOCUMENT)
  actions.put(ActionType.SELECT_NEXT_TOOL_WINDOW, ACTION_SELECT_NEXT_TOOL_WINDOW)
  actions.put(ActionType.SELECT_PREVIOUS_TOOL_WINDOW, ACTION_SELECT_PREVIOUS_TOOL_WINDOW)
  actions.put(ActionType.QUICK_ACCESS, ACTION_QUICK_ACCESS)
  actions.put(ActionType.BUILD_PROJECT, ACTION_BUILD_PROJECT)
  actions.put(ActionType.RUN_PROJECT, ACTION_RUN_PROJECT)
  actions.put(ActionType.OPEN_SETTINGS, ACTION_OPEN_SETTINGS)
  actions.put(ActionType.PRODUCTIVE_MODE, ACTION_PRODUCTIVE_MODE)

  actionsList += ACTION_MOVE_CURSOR_FORWARD
  actionsList += ACTION_MOVE_CURSOR_BACKWARD
  actionsList += ACTION_MOVE_CURSOR_UP
  actionsList += ACTION_MOVE_CURSOR_DOWN
  actionsList += ACTION_SELECT_PREVIOUS_WORD
  actionsList += ACTION_SELECT_NEXT_WORD
  actionsList += ACTION_MOVE_CURSOR_LINE_BEGIN
  actionsList += ACTION_MOVE_CURSOR_LINE_END
  actionsList += ACTION_MOVE_CURSOR_PAGE_UP
  actionsList += ACTION_MOVE_CURSOR_PAGE_DOWN
  actionsList += ACTION_MOVE_CURSOR_FILE_BEGIN
  actionsList += ACTION_MOVE_CURSOR_FILE_END
  actionsList += ACTION_DELETE_FORWARD
  actionsList += ACTION_DELETE_BACKWARD
  actionsList += ACTION_NEW_LINE
  actionsList += ACTION_INSERT_TAB
  actionsList += ACTION_UNDO
  actionsList += ACTION_REDO
  actionsList += ACTION_CUT
  actionsList += ACTION_COPY
  actionsList += ACTION_PASTE
  actionsList += ACTION_SELECT_ALL
  actionsList += ACTION_GOTO_LINE
  actionsList += ACTION_NEW_FILE
  actionsList += ACTION_OPEN_FILE
  actionsList += ACTION_SAVE_FILE
  actionsList += ACTION_SWITCH_TO_NEXT_DOCUMENT
  actionsList += ACTION_SWITCH_TO_PREVIOUS_DOCUMENT
  actionsList += ACTION_SELECT_NEXT_TOOL_WINDOW
  actionsList += ACTION_SELECT_PREVIOUS_TOOL_WINDOW
  actionsList += ACTION_QUICK_ACCESS
  actionsList += ACTION_BUILD_PROJECT
  actionsList += ACTION_RUN_PROJECT
  actionsList += ACTION_OPEN_SETTINGS
  actionsList += ACTION_PRODUCTIVE_MODE
}
