// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.resources

trait ResourceHandler {
  def getGroupName: String

  def getGroupResourceName: String

  def getDefaultResourceName: String

  def getFileResourceExtension: String

  def handleCreate(args: Any*): Resource

  def handleOpen(args: Any*): Resource

  def isProjectHandler: Boolean
}
