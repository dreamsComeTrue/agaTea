// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill

import org.apache.commons.lang3.time.DateFormatUtils

object Log {
  def debug(text: String): Unit = {
    printToConsole(text, "DEBUG")
  }

  def info(text: String): Unit = {
    printToConsole(text, "INFO")
  }

  def warning(text: String): Unit = {
    printToConsole(text, "WARN")
  }

  def error(text: String): Unit = {
    printToConsole(text, "ERROR")
  }

  def raw(text: String): Unit = {
    //    val editor = EditorArea.get.getEditorConsole.getOutputEditor
    //    editor.textEnd()
    //    editor.addNextTextLine(text)
    //    editor.textEnd()
  }

  def printToConsole(text: String, severity: String): Unit = {
    val dateString = DateFormatUtils.format(System.currentTimeMillis, "HH:mm:ss")
    println(severity + ": " + text)
    //    val editor = EditorArea.get.getEditorConsole.getOutputEditor
    //    editor.textEnd()
    //    editor.addNextTextLine(severity + " " + dateString + ": " + text)
    //    editor.textEnd()
  }
}
