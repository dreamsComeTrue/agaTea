// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.resources

abstract class Resource(var name: String) {
  def getName: String = name

  def setName(name: String): Unit = {
    this.name = name
  }

  /**
    * Returns full path to this resource
    *
    * @return full path to this resource
    */
  def getFullPath: String

  /**
    * Sets resource content
    */
  def setContent(content: String): Unit

  /**
    * Returns resource content
    *
    * @return resource content
    */
  def getContent: String

  /**
    * Saves resource to file on disk
    *
    * @param path
    */
  def saveToFile(path: String): Unit
}
