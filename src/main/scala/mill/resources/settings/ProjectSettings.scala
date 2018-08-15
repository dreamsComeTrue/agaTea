// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.resources.settings

import javafx.beans.property.SimpleStringProperty
import javafx.collections.{FXCollections, ObservableList}

class ProjectSettings() {
  private val mainClass = new SimpleStringProperty
  private val vmArguments = new SimpleStringProperty
  private var dependencies = FXCollections.observableArrayList[String]

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

  def getDependencies: ObservableList[String] = dependencies

  def setDependencies(dependencies: ObservableList[String]): Unit = {
    this.dependencies = dependencies
  }
}
