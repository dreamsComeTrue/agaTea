// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill

import java.io.{File, IOException, InputStream}
import java.lang
import java.util.function.Consumer

import javafx.beans.binding.Bindings
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.ListChangeListener
import javafx.event.{Event, EventHandler}
import javafx.scene.control._
import javafx.scene.image.{Image, ImageView}
import javafx.util.Callback
import mill.resources.ResourceHandler
import org.apache.commons.lang3.StringUtils

import scala.collection.mutable.ListBuffer

object Utilities {
  val DEFAULT_IMAGE_PADDING: Double = 4
  private val DOT: Char = '.'
  private val SLASH: Char = '/'
  private val BACKSLASH: Char = '\\'
  private val CLASS_SUFFIX: String = ".class"
  private val BAD_PACKAGE_ERROR: String = "Unable to get resources from path '%s'. Are you sure the given '%s' package exists?"
  private var imagesMap = Map[String, Image]()

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

    if (imagesMap.contains(imagePath)) image = imagesMap(imagePath)
    else {
      image = new Image(getResource(imagePath))
      imagesMap += (imagePath -> image)
    }

    val imageView: ImageView = new ImageView(image)
    imageView.setFitWidth(size)
    imageView.setFitHeight(size)

    imageView
  }

  private def find(file: File, scannedPackage: String): List[Class[_]] = {
    val classes = ListBuffer[Class[_]]()

    if (file.isDirectory) {
      for (nestedFile <- file.listFiles) {
        var path = nestedFile.getAbsolutePath
        path = path.replace(BACKSLASH, DOT).replace(SLASH, DOT)

        val index = path.indexOf(scannedPackage)
        val fullPath = path.substring(index)

        classes ++= find(nestedFile, fullPath)
      }
      //File names with the $1, $2 holds the anonymous inner classes, we are not interested on them.
    }
    else if (scannedPackage.endsWith(CLASS_SUFFIX) && !scannedPackage.contains("$")) {
      val beginIndex = 0
      val endIndex = scannedPackage.length - CLASS_SUFFIX.length
      val className = scannedPackage.substring(beginIndex, endIndex)

      try
        classes += Class.forName(className)
      catch {
        case _: ClassNotFoundException =>
      }
    }
    else if (scannedPackage.endsWith(CLASS_SUFFIX) && scannedPackage.contains("$")) { //   scannedPackage = scannedPackage.replace ("$", ".");
      val beginIndex = 0
      val endIndex = scannedPackage.length - CLASS_SUFFIX.length
      val className = scannedPackage.substring(beginIndex, endIndex)

      try
        classes += Class.forName(className)
      catch {
        case _: ClassNotFoundException =>
      }
    }

    classes.toList
  }

  def findAllClasses(scannedPackage: String): List[Class[_]] = {
    val classLoader = Thread.currentThread.getContextClassLoader
    val scannedPath = scannedPackage.replace(DOT, SLASH)
    val classes = ListBuffer[Class[_]]()

    try {
      val resources = classLoader.getResources(scannedPath)

      while (resources.hasMoreElements) {
        val file = new File(resources.nextElement.getFile)
        classes ++= find(file, scannedPackage)
      }
    }
    catch {
      case e: IOException =>
        throw new IllegalArgumentException(String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage), e)
    }

    classes.toList
  }

  def getAllResourceClasses: List[Class[_]] = {
    val allClasses = findAllClasses("mill.resources")
    val classes = ListBuffer[Class[_]]()

    for (c <- allClasses) {
      val in = c.getInterfaces
      for (i <- in) {
        if (i == classOf[ResourceHandler]) classes += c
      }
    }

    classes.toList
  }

  def getResource(name: String): InputStream = Utilities.getClass.getResourceAsStream("/icons/" + name)

  def isValidIdentifier(text: String): Boolean = {
    for (i <- 0 to text.length) {
      val c = text.charAt(i)
      if ((i == 0 && Character.isDigit(c)) || (!Character.isLetterOrDigit(c) && i > 0 && (c != '_'))) return false
    }
    true
  }

  def makeChangeListener[T](onChangeAction: (ObservableValue[_ <: T], T, T) => Unit): ChangeListener[T] = {
    (observable: ObservableValue[_ <: T], oldValue: T, newValue: T) => {
      onChangeAction(observable, oldValue, newValue)
    }
  }

  def makeListChangeListener[E](onChangedAction: ListChangeListener.Change[_ <: E] => Unit): ListChangeListener[E] = (changeItem: ListChangeListener.Change[_ <: E]) => {
    onChangedAction(changeItem)
  }

  def makeCellFactoryCallback[T](listCellGenerator: ListView[T] => ListCell[T]): Callback[ListView[T], ListCell[T]] = (list: ListView[T]) => listCellGenerator(list)

  def makeEventHandler[E <: Event](f: E => Unit): EventHandler[E] = (e: E) => f(e)

  def makeConsumer[A](function: A => Unit): Consumer[A] = new Consumer[A]() {
    override def accept(arg: A): Unit = function.apply(arg)
  }

}
