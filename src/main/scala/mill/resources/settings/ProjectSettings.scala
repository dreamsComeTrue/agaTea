// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.resources.settings

import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer

class ProjectSettings() {
  private val mainClass = new StringProperty
  private val vmArguments = new StringProperty
  private var dependencies = ObservableBuffer[String]()

  def getMainClass: String = mainClass.get

  def mainClassProperty: StringProperty = mainClass

  def setMainClass(mainClass: String): Unit = {
    this.mainClass.set(mainClass)
  }

  def getVmArguments: String = vmArguments.get

  def vmArgumentsProperty: StringProperty = vmArguments

  def setVmArguments(vmArguments: String): Unit = {
    this.vmArguments.set(vmArguments)
  }

  def getDependencies: ObservableBuffer[String] = dependencies

  def setDependencies(dependencies: ObservableBuffer[String]): Unit = {
    this.dependencies = dependencies
  }
}
