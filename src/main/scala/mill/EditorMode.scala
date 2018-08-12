// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill

import javafx.beans.property.SimpleIntegerProperty

object EditorMode {
  val NORMAL_MODE = 0
  val COMMAND_MODE = 1
  val EDIT_MODE = 2

  var mode = new SimpleIntegerProperty(NORMAL_MODE)
}
