// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.controls

import java.lang

import javafx.animation.Transition
import javafx.beans.property.{BooleanProperty, DoubleProperty, SimpleBooleanProperty, SimpleDoubleProperty}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.control.SplitPane
import javafx.scene.layout.Region
import javafx.util.Duration
import mill.ui.controls.SplitPaneDividerSlider.Direction

object SplitPaneDividerSlider {

  object Direction extends Enumeration {
    type Direction = Value
    val UP, DOWN, LEFT, RIGHT = Value
  }

}

class SplitPaneDividerSlider(val splitPane: SplitPane, val dividerIndex: Int, val direction: SplitPaneDividerSlider.Direction.Direction, var cycleDuration: Duration) {
  init()

  private var slideTransition: Transition = _
  private var aimContentVisibleProperty: BooleanProperty = _
  private var lastDividerPositionProperty: DoubleProperty = _
  private var currentDividerPositionProperty: DoubleProperty = _
  private var content: Region = _
  private var contentInitialMinWidth = .0
  private var contentInitialMinHeight = .0
  private var dividerToMove: SplitPane.Divider = _
  private var handler: EventHandler[ActionEvent] = _

  def this(splitPane: SplitPane, dividerIndex: Int, direction: SplitPaneDividerSlider.Direction.Direction) {
    this(splitPane, dividerIndex, direction, Duration.millis(2000.0))
  }

  private def init(): Unit = {
    slideTransition = new SlideTransition(cycleDuration)

    // figure out right split pane content
    direction match {
      case Direction.LEFT | Direction.UP =>
        content = splitPane.getItems.get(dividerIndex).asInstanceOf[Region]
      case Direction.RIGHT | Direction.DOWN =>
        content = splitPane.getItems.get(dividerIndex + 1).asInstanceOf[Region]
    }

    contentInitialMinHeight = content.getMinHeight
    contentInitialMinWidth = content.getMinWidth

    recomputeSize()

    aimContentVisibleProperty.addListener(new ChangeListener[lang.Boolean] {
      override def changed(observableValue: ObservableValue[_ <: lang.Boolean], oldValue: lang.Boolean, newValue: lang.Boolean): Unit = {
        if (!newValue) {
          // store divider position before transition:
          setLastDividerPosition(splitPane.getDividers.get(dividerIndex).getPosition)
          // "arm" current divider position before transition:
          setCurrentDividerPosition(getLastDividerPosition)
        }

        content.setMinSize(0.0, 0.0)
        slideTransition.play()
      }
    })
  }

  def recomputeSize(): Unit = {
    dividerToMove = splitPane.getDividers.get(dividerIndex)
  }

  private def restoreContentSize(): Unit = {
    content.setMinHeight(contentInitialMinHeight)
    content.setMinWidth(contentInitialMinWidth)

    setCurrentDividerPosition(getLastDividerPosition)
  }

  def getAimContentVisibleProperty: BooleanProperty = {
    if (aimContentVisibleProperty == null) aimContentVisibleProperty = new SimpleBooleanProperty(true)
    aimContentVisibleProperty
  }

  def isAimContentVisible: Boolean = aimContentVisibleProperty.get

  def setAimContentVisible(aimContentVisible: Boolean): Unit = {
    aimContentVisibleProperty.set(aimContentVisible)
  }

  def getLastDividerPositionProperty: DoubleProperty = {
    if (lastDividerPositionProperty == null) lastDividerPositionProperty = new SimpleDoubleProperty
    lastDividerPositionProperty
  }

  def getLastDividerPosition: Double = lastDividerPositionProperty.get

  def setLastDividerPosition(lastDividerPosition: Double): Unit = {
    lastDividerPositionProperty.set(lastDividerPosition)
  }

  def getCurrentDividerPositionProperty: DoubleProperty = {
    if (currentDividerPositionProperty == null) currentDividerPositionProperty = new SimpleDoubleProperty
    currentDividerPositionProperty
  }

  def getCurrentDividerPosition: Double = currentDividerPositionProperty.get

  def setCurrentDividerPosition(currentDividerPosition: Double): Unit = {
    currentDividerPositionProperty.set(currentDividerPosition)
    dividerToMove.setPosition(currentDividerPosition)
  }

  def setEventHandler(value: EventHandler[ActionEvent]): Unit = {
    handler = value
  }

  private class SlideTransition(val cycleDuration: Duration) extends Transition {
    setCycleDuration(cycleDuration)

    override protected def interpolate(d: Double): Unit = {
      direction match {
        case Direction.LEFT | Direction.UP =>
          // intent to slide in content:
          if (isAimContentVisible) {
            if ((getCurrentDividerPosition + d) <= getLastDividerPosition) setCurrentDividerPosition(getCurrentDividerPosition + d)
            else { //DONE
              restoreContentSize()
              stop()

              if (handler != null) handler.handle(new ActionEvent)
            }
          } // intent to slide out content:
          else {
            if (getCurrentDividerPosition > 0.0) setCurrentDividerPosition(getCurrentDividerPosition - d)
            else {
              setCurrentDividerPosition(0.0)
              stop()
              if (handler != null) handler.handle(new ActionEvent)
            }
          }
        case Direction.RIGHT | Direction.DOWN =>
          if (isAimContentVisible) {
            if ((getCurrentDividerPosition - d) >= getLastDividerPosition) setCurrentDividerPosition(getCurrentDividerPosition - d)
            else {
              restoreContentSize()
              stop()

              if (handler != null) handler.handle(new ActionEvent)
            }
          }
          else if (getCurrentDividerPosition < 1.0) setCurrentDividerPosition(getCurrentDividerPosition + d)
          else {
            setCurrentDividerPosition(1.0)
            stop()
            if (handler != null) handler.handle(new ActionEvent)
          }
      }
    }
  }
}
