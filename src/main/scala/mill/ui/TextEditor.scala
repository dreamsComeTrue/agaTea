// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import java.time.Duration
import java.util
import java.util.Collections
import java.util.concurrent.{ExecutorService, Executors}
import java.util.function.{Consumer, IntFunction}
import java.util.regex.Pattern

import javafx.beans.value.ObservableValue
import javafx.concurrent.Task
import javafx.event.Event
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout._
import javafx.scene.paint.Color
import javafx.scene.shape.Polygon
import org.fxmisc.flowless.VirtualizedScrollPane
import org.fxmisc.richtext.model.{StyleSpans, StyleSpansBuilder}
import org.fxmisc.richtext.{CodeArea, LineNumberFactory}
import org.fxmisc.wellbehaved.event.{EventPattern, InputMap, Nodes}
import org.reactfx.value.Val

import scala.language.implicitConversions

class TextEditor(val tabName: String, val text: String, val path: String) extends AnchorPane {

  private val KEYWORDS =
    util.Arrays.asList("abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "def", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "object", "override", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "val", "var", "volatile", "while")

  private val KEYWORD_PATTERN = "\\b(" + java.lang.String.join("|", KEYWORDS) + ")\\b"
  private val PAREN_PATTERN = "\\(|\\)"
  private val BRACE_PATTERN = "\\{|\\}"
  private val BRACKET_PATTERN = "\\[|\\]"
  private val SEMICOLON_PATTERN = "\\;"
  private val STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\""
  private val COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/"

  private val PATTERN = Pattern.compile("(?<KEYWORD>" + KEYWORD_PATTERN + ")" + "|(?<PAREN>" + PAREN_PATTERN + ")" + "|(?<BRACE>" + BRACE_PATTERN + ")" + "|(?<BRACKET>" + BRACKET_PATTERN + ")" + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")" + "|(?<STRING>" + STRING_PATTERN + ")" + "|(?<COMMENT>" + COMMENT_PATTERN + ")")


  private var executor: ExecutorService = _
  val codeAreaVirtual: VirtualizedScrollPane[CodeArea] = createCodeArea(text)

  implicit def toConsumer[A](function: A => Unit): Consumer[A] = (arg: A) => function.apply(arg)

  private def changeKeyTabSize(codeArea: CodeArea): Unit = {
    val eventPattern: EventPattern[Event, KeyEvent] = EventPattern.keyPressed(KeyCode.TAB)
    val consumer = new Consumer[KeyEvent]() {
      override def accept(t: KeyEvent): Unit = {
        codeArea.replaceSelection("    ")
      }
    }

    val im: InputMap[KeyEvent] = InputMap.consume(eventPattern, consumer)
    Nodes.addInputMap(codeArea, im)
  }

  private def createCodeArea(text: String): VirtualizedScrollPane[CodeArea] = {
    executor = Executors.newSingleThreadExecutor

    val codeArea: CodeArea = new CodeArea()
    val graphicFactory: IntFunction[Node] = (line: Int) => {
      def foo(line: Int) = {
        val arrowMargin: IntFunction[Node] = new CodeAreaArrowMargin(codeArea.currentParagraphProperty)
        val numberFactory: IntFunction[Node] = LineNumberFactory.get(codeArea)
        val hBox = new HBox(numberFactory.apply(line), arrowMargin.apply(line))
        hBox.setAlignment(Pos.CENTER_LEFT)
        hBox
      }

      foo(line)
    }

    codeArea.setParagraphGraphicFactory(graphicFactory)
    codeArea.setWrapText(true)
    codeArea.getStyleClass.add("codeArea")
    codeArea.displaceCaret(0)

    //  Set async syntax highlighting
    codeArea
      .multiPlainChanges()
      .successionEnds(Duration.ofMillis(50))
      .supplyTask(() => computeHighlightingAsync)
      .awaitLatest(codeArea.multiPlainChanges())
      .filterMap(t => t.toOptional)
      .subscribe(applyHighlighting _)

    //  Fix 'Tab' key to insert 4 spaces
    changeKeyTabSize(codeArea)

    codeArea.appendText(text)

    val result = new VirtualizedScrollPane(codeArea)

    AnchorPane.setTopAnchor(result, 0.0)
    AnchorPane.setBottomAnchor(result, 0.0)
    AnchorPane.setLeftAnchor(result, 0.0)
    AnchorPane.setRightAnchor(result, 0.0)

    this.getChildren.add(result)

    result
  }

  def setText(text: String): Unit = {
    codeAreaVirtual.getContent.replaceText(0, codeAreaVirtual.getContent.getLength, text)
  }

  def getText: String = {
    codeAreaVirtual.getContent.getText()
  }

  private def computeHighlightingAsync: Task[StyleSpans[util.Collection[String]]] = {
    val text = codeAreaVirtual.getContent.getText()
    val task = new Task[StyleSpans[util.Collection[String]]]() {
      @throws[Exception]
      protected def call: StyleSpans[util.Collection[String]] = computeHighlighting(text)
    }

    executor.execute(task)

    task
  }

  private def applyHighlighting(highlighting: StyleSpans[util.Collection[String]]): Unit = {
    codeAreaVirtual.getContent.setStyleSpans(0, highlighting)
  }

  private def computeHighlighting(text: String): StyleSpans[util.Collection[String]] = {
    val matcher = PATTERN.matcher(text)
    var lastKwEnd = 0
    val spansBuilder = new StyleSpansBuilder[util.Collection[String]]
    while (matcher.find) {
      val styleClass = if (matcher.group("KEYWORD") != null) "keyword"
      else if (matcher.group("PAREN") != null) "paren"
      else if (matcher.group("BRACE") != null) "brace"
      else if (matcher.group("BRACKET") != null) "bracket"
      else if (matcher.group("SEMICOLON") != null) "semicolon"
      else if (matcher.group("STRING") != null) "string"
      else if (matcher.group("COMMENT") != null) "comment"
      else null

      // never happens
      assert(styleClass != null)
      spansBuilder.add(Collections.unmodifiableCollection(Collections.emptyList()), matcher.start - lastKwEnd)
      spansBuilder.add(Collections.singleton(styleClass), matcher.end - matcher.start)
      lastKwEnd = matcher.end
    }

    spansBuilder.add(Collections.unmodifiableCollection(Collections.emptyList()), text.length - lastKwEnd)
    spansBuilder.create
  }
}

class CodeAreaArrowMargin(val shownLine: ObservableValue[Integer]) extends IntFunction[Node] {
  override def apply(lineNumber: Int): Node = {
    val triangle = new Polygon(0.0, 0.0, 10.0, 5.0, 0.0, 10.0)
    triangle.setFill(Color.LIGHTGREEN)

    val visible: ObservableValue[java.lang.Boolean] = Val.map(shownLine, (sl: Integer) => sl.equals(lineNumber))

    triangle.visibleProperty.bind(visible)

    triangle
  }
}