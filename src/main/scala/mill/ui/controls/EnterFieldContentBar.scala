// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.controls

import javafx.event.ActionEvent
import javafx.geometry.{HPos, Insets, Pos}
import javafx.scene.control.{Button, Label, TextField, Tooltip}
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.{ColumnConstraints, GridPane, Priority, StackPane}
import mill.controller.AppController
import mill.{Resources, Utilities}

class EnterFieldContentBar() {
  private val content = new GridPane
  private val validationLabel = new Label(Resources.ENTERED_INVALID_PACKAGE_NAME)
  private var okButton: Button = _
  private var cancelButton: Button = _
  private val textField = new TextField
  private var label = new Label

  init()

  private def init() {
    validationLabel.getStyleClass.add("validation-error")

    GridPane.setMargin(validationLabel, new Insets(5, 0, 0, 0))
    GridPane.setMargin(textField, new Insets(0, 5, 0, 0))

    okButton = Utilities.createButton(Resources.Images.IMAGE_ACCEPT, 20.0, Utilities.DEFAULT_IMAGE_PADDING)
    okButton.setTooltip(new Tooltip(Resources.ACCEPT))

    cancelButton = Utilities.createButton(Resources.Images.IMAGE_REJECT, 20.0, Utilities.DEFAULT_IMAGE_PADDING)
    cancelButton.setTooltip(new Tooltip(Resources.REJECT))
    cancelButton.setOnAction((_: ActionEvent) => AppController.instance().hideContentBar())

    content.add(label, 0, 0, 1, 2)
    content.add(textField, 1, 0, 1, 2)
    content.add(okButton, 2, 0)
    content.add(cancelButton, 2, 1)

    GridPane.setHalignment(validationLabel, HPos.CENTER)

    content.setAlignment(Pos.CENTER_LEFT)
    content.setMaxWidth(380)

    val column1 = new ColumnConstraints(130)
    val column2 = new ColumnConstraints(230)
    val column3 = new ColumnConstraints(20)
    column2.setHgrow(Priority.ALWAYS)

    content.getColumnConstraints.addAll(column1, column2, column3)
    textField.setOnKeyPressed((event: KeyEvent) => {
      if (event.getCode eq KeyCode.ENTER) okButton.fire()
    })
  }

  def show(iconPath: String): Unit = {
    hideValidationText()
    AppController.instance().setContentBarHeight(60)
    AppController.instance().showContentBar(iconPath, new StackPane(content), textField)
  }

  def showValidationText(text: String): Unit = {
    if (!content.getChildren.contains(validationLabel)) {
      validationLabel.setText(text)
      content.add(validationLabel, 0, 2, 3, 1)

      AppController.instance().setContentBarHeight(80)

      okButton.setVisible(false)

      GridPane.setRowIndex(cancelButton, 0)
      GridPane.setRowSpan(cancelButton, 2)
    }
  }

  def hideValidationText(): Unit = {
    if (content.getChildren.contains(validationLabel)) {
      content.getChildren.remove(validationLabel)

      AppController.instance().setContentBarHeight(60)

      okButton.setVisible(true)

      GridPane.setRowIndex(cancelButton, 1)
      GridPane.setRowSpan(cancelButton, 1)
    }
  }

  def getOkButton: Button = okButton

  def getCancelButton: Button = cancelButton

  def getTextField: TextField = textField

  def getLabel: Label = label

  def setLabel(label: Label): Unit = {
    this.label = label
  }
}

