// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.controller

trait FXStageInitializer {
  /**
    * Returns true when we are done with initializing and we wish
    * to remove this initializer from AppController runs
    *
    * @return
    */
  def fxInitialize: Boolean
}
