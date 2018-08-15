// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.controller

object FlowState extends Enumeration {
  type FlowState = Value
  val //  Standard view with code editor and project explorer
  APPLICATION_PROJECT, //  UML-like view
  APPLICATION_STRUCTURE, //  New resource dialog open
  NEW_RESOURCE, //  Open resource dialog
  OPEN_RESOURCE, //  Application settings view
  SETTINGS = Value
}