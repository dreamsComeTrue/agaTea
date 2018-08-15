// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill

import java.util.function.Consumer

import javafx.animation.{FadeTransition, ParallelTransition, ScaleTransition}
import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.util.Duration

abstract class ViewTransition(var fromView: Node, var toView: Node) {
  private var ft: FadeTransition = _
  private var s: ScaleTransition = _
  private var ft2: FadeTransition = _
  private var s2: ScaleTransition = _
  private var ft_end: FadeTransition = _
  private var s_end: ScaleTransition = _
  private var ft2_end: FadeTransition = _
  private var s2_end: ScaleTransition = _
  private var pt: ParallelTransition = _
  private var pt2: ParallelTransition = _
  private var pt_end: ParallelTransition = _
  private var pt2_end: ParallelTransition = _
  var onFirstViewStartFunc: Consumer[Void] = _
  var onFirstViewCompletedFunc: Consumer[Void] = _
  var onFirstTransitionFinishedFunc: Consumer[Void] = _
  var onSecondViewCompletedFunc: Consumer[Void] = _

  init()

  def init(): Unit = {
    val deltaTime = 400.0

    ft = new FadeTransition(Duration.millis(deltaTime), fromView)
    ft.setFromValue(0.0)
    ft.setToValue(1.0)

    s = new ScaleTransition(Duration.millis(deltaTime), fromView)
    s.setToX(1.0)
    s.setToY(1.0)

    ft2 = new FadeTransition(Duration.millis(deltaTime), toView)
    ft2.setFromValue(1.0)
    ft2.setToValue(0.0)

    s2 = new ScaleTransition(Duration.millis(deltaTime), toView)
    s2.setToX(0.0)
    s2.setToY(0.0)

    ft_end = new FadeTransition(Duration.millis(deltaTime), fromView)
    ft_end.setFromValue(1.0)
    ft_end.setToValue(0.0)

    s_end = new ScaleTransition(Duration.millis(deltaTime), fromView)
    s_end.setToX(0.0)
    s_end.setToY(0.0)

    ft2_end = new FadeTransition(Duration.millis(deltaTime), toView)
    ft2_end.setFromValue(0.0)
    ft2_end.setToValue(1.0)

    s2_end = new ScaleTransition(Duration.millis(deltaTime), toView)
    s2_end.setToX(1.0)
    s2_end.setToY(1.0)

    pt = new ParallelTransition(fromView, ft, s)
    pt2 = new ParallelTransition(toView, ft2, s2)
    pt_end = new ParallelTransition(fromView, ft_end, s_end)
    pt2_end = new ParallelTransition(toView, ft2_end, s2_end)

    pt.setOnFinished((_: ActionEvent) => {
      onFirstTransitionFinished()
      onFirstTransitionFinishedFunc.accept(null)
    })

    pt_end.setOnFinished((_: ActionEvent) => onSecondViewCompletedFunc.accept(null))

    reset()
  }

  def onFirstViewStart(): Unit

  def onFirstViewCompleted(): Unit

  def onFirstTransitionFinished(): Unit

  def onSecondViewCompleted(): Unit

  def reset(): Unit = {
    onFirstViewStartFunc = null
    onFirstViewCompletedFunc = null
    onFirstTransitionFinishedFunc = null
    onSecondViewCompletedFunc = null
  }

  def setFromView(fromView: Node): Unit = {
    this.fromView = fromView

    ft.setNode(fromView)
    ft.setFromValue(0.0)
    ft.setToValue(1.0)

    s.setNode(fromView)
    s.setToX(1.0)
    s.setToY(1.0)

    ft_end.setNode(fromView)
    ft_end.setFromValue(1.0)
    ft_end.setToValue(0.0)

    s_end.setNode(fromView)
    s_end.setToX(0.0)
    s_end.setToY(0.0)

    pt = new ParallelTransition(fromView, ft, s)
    pt_end = new ParallelTransition(fromView, ft_end, s_end)

    pt.setOnFinished((_: ActionEvent) => {
      onFirstTransitionFinished()
      if (onFirstTransitionFinishedFunc != null) onFirstTransitionFinishedFunc.accept(null)
      pt.stop()
    })

    pt_end.setOnFinished((_: ActionEvent) => {
        if (onSecondViewCompletedFunc != null) onSecondViewCompletedFunc.accept(null)
        pt_end.stop()
    })

    reset()
  }

  def setToView(toView: Node): Unit = {
    this.toView = toView

    ft2.setNode(toView)
    ft2.setFromValue(1.0)
    ft2.setToValue(0.0)

    s2.setNode(toView)
    s2.setToX(0.0)
    s2.setToY(0.0)

    ft2_end.setNode(toView)
    ft2_end.setFromValue(0.0)
    ft2_end.setToValue(1.0)

    s2_end.setNode(toView)
    s2_end.setToX(1.0)
    s2_end.setToY(1.0)

    pt2 = new ParallelTransition(toView, ft2, s2)
    pt2_end = new ParallelTransition(toView, ft2_end, s2_end)

    pt2.setOnFinished((_: ActionEvent) => pt2.stop())
    pt2_end.setOnFinished((_: ActionEvent) => pt2_end.stop())

    reset()
  }

  def runTransition(enable: Boolean): Unit = {
    if (enable) {
      fromView.setOpacity(0.0)
      fromView.setScaleX(0.0)
      fromView.setScaleY(0.0)

      toView.setOpacity(1.0)
      toView.setScaleX(1.0)
      toView.setScaleY(1.0)

      if (onFirstViewStartFunc != null) onFirstViewStartFunc.accept(null)

      onFirstViewStart()

      pt.playFromStart()
      pt2.playFromStart()

      if (onFirstViewCompletedFunc != null) onFirstViewCompletedFunc.accept(null)

      onFirstViewCompleted()
    }
    else {
      fromView.setOpacity(1.0)
      fromView.setScaleX(1.0)
      fromView.setScaleY(1.0)

      toView.setOpacity(0.0)
      toView.setScaleX(0.0)
      toView.setScaleY(0.0)

      pt_end.playFromStart()
      pt2_end.playFromStart()

      onSecondViewCompleted()
    }
  }
}

