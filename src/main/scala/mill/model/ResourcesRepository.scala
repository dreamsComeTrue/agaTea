// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.model

import javafx.collections.{FXCollections, ObservableList}
import mill.resources.ResourceHandler

class ResourcesRepository private() {
  private val resourceHandlers = FXCollections.observableArrayList[ResourceHandler]

  def getResourceHandlers: ObservableList[ResourceHandler] = resourceHandlers
}

object ResourcesRepository {
  private var _instance: ResourcesRepository = _

  def instance(): ResourcesRepository = {
    if (_instance == null) _instance = new ResourcesRepository

    _instance
  }
}
