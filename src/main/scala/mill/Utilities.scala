// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill

import java.io.InputStream
import java.{lang, util}

import javafx.beans.binding.Bindings
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.scene.control._
import javafx.scene.image.{Image, ImageView}
import org.apache.commons.lang3.StringUtils

object Utilities {
  val DEFAULT_IMAGE_PADDING: Double = 4
  private val DOT: Char = '.'
  private val SLASH: Char = '/'
  private val BACKSLASH: Char = '\\'
  private val CLASS_SUFFIX: String = ".class"
  private val BAD_PACKAGE_ERROR: String = "Unable to get resources from path '%s'. Are you sure the given '%s' package exists?"
  private var imagesMap: util.HashMap[String, Image] = _

  def createButton(imagePath: String, size: Double, imagePadding: Double): Button = {
    val image: Image = new Image(getResource(imagePath))
    val imageView: ImageView = new ImageView(image)
    val button: Button = new Button("", imageView)

    button.setMaxSize(size, size)
    button.setPrefSize(size, size)
    button.setMinSize(size, size)
    imageView.fitWidthProperty.bind(Bindings.subtract(button.widthProperty, imagePadding))
    imageView.fitHeightProperty.bind(Bindings.subtract(button.heightProperty, imagePadding))

    button
  }

  def createOnOffButton(onImagePath: String, offImagePath: String, size: Double, imagePadding: Double): ToggleButton = {
    val offImage: Image = new Image(Utilities.getResource(offImagePath))
    val onImage: Image = new Image(Utilities.getResource(onImagePath))
    val imageView: ImageView = new ImageView(offImage)
    val button: ToggleButton = new ToggleButton("", imageView)

    button.setTooltip(new Tooltip(Resources.EXPAND_TO_SOURCE))
    button.setMaxSize(size, size)
    button.setPrefSize(size, size)
    button.setMinSize(size, size)

    imageView.fitWidthProperty.bind(Bindings.subtract(button.widthProperty, imagePadding))
    imageView.fitHeightProperty.bind(Bindings.subtract(button.heightProperty, imagePadding))
    button.selectedProperty.addListener(new ChangeListener[lang.Boolean] {
      override def changed(observable: ObservableValue[_ <: lang.Boolean], oldValue: lang.Boolean, newValue: lang.Boolean): Unit = {
        if (newValue) imageView.setImage(onImage)
        else imageView.setImage(offImage)
      }
    })

    button
  }

  def createToggleButton(imagePath: String, size: Double, imagePadding: Double): ToggleButton = {
    val image: Image = new Image(getResource(imagePath))
    val imageView: ImageView = new ImageView(image)
    val button: ToggleButton = new ToggleButton("", imageView)

    button.setMaxSize(size, size)
    button.setPrefSize(size, size)
    button.setMinSize(size, size)
    imageView.fitWidthProperty.bind(Bindings.subtract(button.widthProperty, imagePadding))
    imageView.fitHeightProperty.bind(Bindings.subtract(button.heightProperty, imagePadding))

    button
  }

  def createImageView(imagePath: String, size: Double): ImageView = {
    if (StringUtils.isEmpty(imagePath)) return null

    var image: Image = null

    if (imagesMap.containsKey(imagePath)) image = imagesMap.get(imagePath)
    else {
      image = new Image(getResource(imagePath))
      imagesMap.put(imagePath, image)
    }

    val imageView: ImageView = new ImageView(image)
    imageView.setFitWidth(size)
    imageView.setFitHeight(size)

    imageView
  }

  def getResource(name: String): InputStream = Utilities.getClass.getResourceAsStream("/icons/" + name)
}
