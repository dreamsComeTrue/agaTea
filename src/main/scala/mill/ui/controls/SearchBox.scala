// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.controls

import java.lang

import javafx.animation.{KeyFrame, Timeline}
import javafx.application.Platform
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.ActionEvent
import javafx.geometry.{Point2D, Side}
import javafx.scene.control._
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.{HBox, Region, VBox}
import javafx.stage.{Popup, WindowEvent}
import javafx.util.Duration
import javax.naming.directory.SearchResult
import org.w3c.dom.DocumentType

class SearchBox() extends Region {
  private var textBox: TextField = _
  private var clearButton: Button = _
  private val contextMenu: ContextMenu = new ContextMenu
  private val extraInfoPopup: Popup = new Popup
  private var infoBox: VBox = _
  private val searchErrorTooltip: Tooltip = new Tooltip
  private var searchErrorTooltipHidder: Timeline = _

  init()

  def init(): Unit = {
    setId("search-box")
    getStyleClass.add("search-box")
    setPrefSize(160, 20)
    setMaxHeight(20)

    textBox = new TextField
    textBox.setPromptText("Search")
    textBox.setFocusTraversable(false)

    textBox.focusedProperty.addListener(new ChangeListener[lang.Boolean] {
      override def changed(observableValue: ObservableValue[_ <: lang.Boolean], oldValue: lang.Boolean, newValue: lang.Boolean): Unit = {
        if (!newValue) textBox.setText("")
      }
    })
    textBox.setOnKeyReleased((keyEvent: KeyEvent) => {
      if (keyEvent.getCode eq KeyCode.DOWN) contextMenu.setFocused(true)
    })

    clearButton = new Button
    clearButton.setVisible(false)
    clearButton.setOnAction((_: ActionEvent) => {
      textBox.setText("")
      textBox.requestFocus()
    })

    getChildren.addAll(textBox, clearButton)

    textBox.textProperty.addListener(new ChangeListener[String] {
      override def changed(observableValue: ObservableValue[_ <: String], oldValue: String, newValue: String): Unit = {
        clearButton.setVisible(textBox.getText.length != 0)
        if (textBox.getText.length == 0) {
          if (contextMenu != null) contextMenu.hide()
          showError(null)
        }
        else {
          val haveResults: Boolean = false
          val results: Map[DocumentType, List[SearchResult]] = null
          //	try
          //			if (indexSearcher == null)

          //indexSearcher = new IndexSearcher (null, nul);

          //results = indexSearcher.search (textBox.getText () + (textBox.getText ().matches ("\\w+") ? "*" : ""));
          // check if we have any results
          //					for (List<SearchResult> categoryResults : results.values ())
          //			if (categoryResults.size () > 0)

          //				haveResults = true;
          //				break;


          //					showError (e.getMessage ().substring ("Cannot parse ".length ()));

          if (haveResults) {
            showError(null)
            populateMenu(results)
            if (!contextMenu.isShowing) contextMenu.show(SearchBox.this, Side.BOTTOM, 10, -5)
          }
          else {
            if (searchErrorTooltip.getText == null) showError("No matches")
            contextMenu.hide()
          }
          contextMenu.setFocused(true)
        }
      }
    })

    // create info popup
    infoBox = new VBox
    infoBox.setId("search-info-box")
    infoBox.setFillWidth(true)
    infoBox.setMinWidth(Region.USE_PREF_SIZE)
    infoBox.setPrefWidth(350)

    val infoName: Label = new Label
    infoName.setId("search-info-name")
    infoName.setMinHeight(Region.USE_PREF_SIZE)
    infoName.setPrefHeight(28)

    val infoDescription: Label = new Label
    infoDescription.setId("search-info-description")
    infoDescription.setWrapText(true)
    infoDescription.setPrefWidth(infoBox.getPrefWidth - 24)

    infoBox.getChildren.addAll(infoName, infoDescription)
    extraInfoPopup.getContent.add(infoBox)

    // hide info popup when context menu is hidden
    contextMenu.setOnHidden((_: WindowEvent) => extraInfoPopup.hide())
  }

  private def showError(message: String): Unit = {
    searchErrorTooltip.setText(message)

    if (searchErrorTooltipHidder != null) searchErrorTooltipHidder.stop()

    if (message != null) {
      val toolTipPos: Point2D = textBox.localToScene(0, textBox.getLayoutBounds.getHeight)
      val x: Double = toolTipPos.getX + textBox.getScene.getX + textBox.getScene.getWindow.getX
      val y: Double = toolTipPos.getY + textBox.getScene.getY + textBox.getScene.getWindow.getY

      searchErrorTooltip.show(textBox.getScene.getWindow, x, y)
      searchErrorTooltipHidder = new Timeline
      searchErrorTooltipHidder.getKeyFrames.add(new KeyFrame(Duration.seconds(3), (t: ActionEvent) => {
        searchErrorTooltip.hide()
        searchErrorTooltip.setText(null)
      }))

      searchErrorTooltipHidder.play()
    }
    else searchErrorTooltip.hide()
  }

  private def populateMenu(results: Map[DocumentType, List[SearchResult]]): Unit = {
    contextMenu.getItems.clear()

    for (entry <- results) {
      var first: Boolean = true

      for (result <- entry._2) {
        val sr: SearchResult = result
        val hBox: HBox = new HBox

        hBox.setFillHeight(true)

        val itemLabel: Label = new Label(result.getName)
        itemLabel.getStyleClass.add("item-label")

        if (first) {
          first = false
          //		Label groupLabel = new Label (result.getDocumentType ().getPluralDisplayName ());
          //		groupLabel.getStyleClass ().add ("group-label");
          //	groupLabel.setAlignment (Pos.CENTER_RIGHT);
          //	groupLabel.setMinWidth (USE_PREF_SIZE);
          //	groupLabel.setPrefWidth (70);
          //hBox.getChildren ().addAll (groupLabel, itemLabel);
        }
        else {
          val spacer: Region = new Region
          spacer.setMinWidth(Region.USE_PREF_SIZE)
          spacer.setPrefWidth(70)
          hBox.getChildren.addAll(spacer, itemLabel)
        }

        // create a special node for hiding/showing popup content
        val popRegion: Region = new Region
        popRegion.getStyleClass.add("search-menu-item-popup-region")
        popRegion.setPrefSize(10, 10)
        hBox.getChildren.add(popRegion)
        //	final String name = (result.getDocumentType () == DocumentType.SAMPLE) ? result.getName () :
        //			result.getPackageName () +
        //					((result.getClassName () != null) ? "." + result.getClassName () : "") +
        //					((result.getName () != null) ? "." + result.getName () : "");
        //	final String shortDescription = (result.getShortDescription ().length () == 160) ? result.getShortDescription () + "..." : result.getShortDescription ();
        popRegion.opacityProperty.addListener(new ChangeListener[Number] {
          override def changed(observableValue: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
            Platform.runLater(() => {
              if (popRegion.getOpacity == 1) { //			infoName.setText (name);
                //			infoDescription.setText (shortDescription);
                val hBoxPos: Point2D = hBox.localToScene(0, 0)
                extraInfoPopup.show(getScene.getWindow, hBoxPos.getX + contextMenu.getScene.getX + contextMenu.getX - infoBox.getPrefWidth - 10, hBoxPos.getY + contextMenu.getScene.getY + contextMenu.getY - 27)
              }
            })
          }
        })

        // create menu item
        val menuItem: CustomMenuItem = new CustomMenuItem(hBox, true)
        menuItem.getStyleClass.add("search-menu-item")

        contextMenu.getItems.add(menuItem)

        // handle item selection
        menuItem.setOnAction((actionEvent: ActionEvent) => System.out.println("akcja!"))
      }
    }
  }

  override protected def layoutChildren(): Unit = {
    textBox.resize(getWidth, getHeight)
    clearButton.resizeRelocate(getWidth - 18, 4, 12, 13)
  }

  def setPromptText(text: String): Unit = {
    textBox.setPromptText(text)
  }

  override def requestFocus(): Unit = {
    textBox.requestFocus()
  }

  def getTextBox: TextField = textBox
}
