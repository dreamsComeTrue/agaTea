// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui

import java.io.File

import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.control.{Label, Tab, TabPane}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout._

import scala.io.Source

class ContentPane(val mainFrame: MainFrame) extends StackPane {
  setId("center-pane")
  createLogo()

  private var tabPane = createTabPane()

  private def createLogo() = {
    val label = new Label("mill")
    label.setId("center-logo")

    val image = new Image(getClass.getResourceAsStream("/logo.png"))

    val centerImage = new ImageView()
    centerImage.setImage(image)
    centerImage.setFitWidth(240)
    centerImage.setPreserveRatio(true)
    centerImage.setSmooth(true)

    this.getChildren.add(centerImage)
  }

  private def createTabPane(): TabPane = {
    val tabPane = new TabPane()

    tabPane.getSelectionModel.selectedItemProperty().addListener(new ChangeListener[Tab] {
      override def changed(observableValue: ObservableValue[_ <: Tab], t: Tab, t1: Tab): Unit = {
        if (observableValue.getValue != null) {
          val textEditor = observableValue.getValue.getContent.asInstanceOf[TextEditor]
          mainFrame.assignCurrentTextEditor(Option(textEditor))
        } else {
          mainFrame.assignCurrentTextEditor(None)
        }
      }
    })

    this.getChildren.add(tabPane)

    tabPane
  }

  def addTab(header: String, text: String, path: String = ""): Unit = {
    val textEditor = new TextEditor(header, text, path)

    val tab = new Tab()
    tab.setText(header)
    tab.setContent(textEditor)

    tabPane.getTabs.add(tab)
    tabPane.getSelectionModel.select(tab)

    mainFrame.assignCurrentTextEditor(Option(textEditor))
  }


  def addTab(file: File): Unit = {
    val header = file.getName
    val text = Source.fromFile(file).mkString

    addTab(header, text, file.getAbsolutePath)
  }

}
