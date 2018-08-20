// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.resources.settings

import javafx.beans.property.SimpleStringProperty
import scalafx.collections.ObservableBuffer

class ProjectSettings() {
  private val mainClass = new SimpleStringProperty
  private val vmArguments = new SimpleStringProperty
  private var dependencies = ObservableBuffer[String]()

  def getMainClass: String = mainClass.get

  def mainClassProperty: SimpleStringProperty = mainClass

  def setMainClass(mainClass: String): Unit = {
    this.mainClass.set(mainClass)
  }

  def getVmArguments: String = vmArguments.get

  def vmArgumentsProperty: SimpleStringProperty = vmArguments

  def setVmArguments(vmArguments: String): Unit = {
    this.vmArguments.set(vmArguments)
  }

  def getDependencies: ObservableBuffer[String] = dependencies

  def setDependencies(dependencies: ObservableBuffer[String]): Unit = {
    this.dependencies = dependencies
  }
}
