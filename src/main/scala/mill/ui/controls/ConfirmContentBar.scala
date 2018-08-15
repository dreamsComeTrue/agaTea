// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.controls

import javafx.event.ActionEvent
import javafx.geometry.{Insets, Pos}
import javafx.scene.control.{Button, Label, Tooltip}
import javafx.scene.layout.{HBox, VBox}
import mill.controller.AppController
import mill.{Resources, Utilities}

class ConfirmContentBar() {
  private val content = new VBox
  private var okButton: Button = _
  private var cancelButton: Button = _
  private var captionLabel = new Label
  private var messageLabel = new Label

  init()

  private def init() {
    messageLabel.setStyle("-fx-font-weight: bolder;")
    okButton = Utilities.createButton(Resources.Images.IMAGE_ACCEPT, 20.0, Utilities.DEFAULT_IMAGE_PADDING)
    cancelButton = Utilities.createButton(Resources.Images.IMAGE_REJECT, 20.0, Utilities.DEFAULT_IMAGE_PADDING)
    cancelButton.setTooltip(new Tooltip(Resources.REJECT))
    cancelButton.setOnAction((_: ActionEvent) => AppController.instance().hideContentBar())

    val buttonsBox = new HBox(okButton, cancelButton)
    buttonsBox.setSpacing(10.0)
    buttonsBox.setAlignment(Pos.CENTER)

    VBox.setMargin(captionLabel, new Insets(5.0, 0.0, 5.0, 0.0))
    VBox.setMargin(messageLabel, new Insets(5.0, 0.0, 10.0, 0.0))
    VBox.setMargin(buttonsBox, new Insets(0.0, 0.0, 5.0, 0.0))

    content.getChildren.addAll(captionLabel, messageLabel, buttonsBox)
    content.setAlignment(Pos.CENTER)
  }

  def show(iconImagePath: String): Unit = {
    val controller = AppController.instance()
    controller.setContentBarHeight(85)
    controller.showContentBar(iconImagePath, content, okButton)
  }

  def getOkButton: Button = okButton

  def getCancelButton: Button = cancelButton

  def getCaptionLabel: Label = captionLabel

  def setCaptionLabel(captionLabel: Label): Unit = {
    this.captionLabel = captionLabel
  }

  def getMessageLabel: Label = messageLabel

  def setMessageLabel(messageLabel: Label): Unit = {
    this.messageLabel = messageLabel
  }
}
