// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.model

import mill.resources.ResourceHandler
import scalafx.collections.ObservableBuffer

class ResourcesRepository private() {
  private val resourceHandlers = new ObservableBuffer[ResourceHandler]()

  def getResourceHandlers: ObservableBuffer[ResourceHandler] = resourceHandlers
}

object ResourcesRepository {
  private var _instance: ResourcesRepository = _

  def instance(): ResourcesRepository = {
    if (_instance == null) _instance = new ResourcesRepository

    _instance
  }
}
