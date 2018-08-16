// Copyright 2018 Dominik 'dreamsComeTrue' Jasiński. All Rights Reserved.

package mill.ui.controls

import javafx.beans.binding.NumberBinding
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.ListChangeListener
import javafx.scene.control.{TreeTableColumn, TreeTableView}
import javafx.scene.layout.{ColumnConstraints, GridPane, StackPane}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object PercentageTreeTableView {

  class PercentageTableColumn[s, t](val columnName: String) extends TreeTableColumn[s, t](columnName) {
    private val percentWidth = new SimpleDoubleProperty

    def getPercentWidth: SimpleDoubleProperty = percentWidth

    def setPercentWidth(percentWidth: Double): Unit = {
      this.percentWidth.set(percentWidth)
    }
  }

}

class PercentageTreeTableView[s] @SuppressWarnings(Array("rawtypes"))() extends StackPane {
  private val table = new TreeTableView[s]
  val grid = new GridPane

  this.table.getColumns.addListener(new ListChangeListener[TreeTableColumn[s, _]] {
    override def onChanged(change: ListChangeListener.Change[_ <: TreeTableColumn[s, _]]): Unit = {
      grid.getColumnConstraints.clear()
      val arr1 = ListBuffer[ColumnConstraints]()
      val arr2 = ListBuffer[StackPane]()

      var i = 0
      for (column <- PercentageTreeTableView.this.table.getColumns.asScala) {
        val col: PercentageTreeTableView.PercentageTableColumn[_, _] = column.asInstanceOf[PercentageTreeTableView.PercentageTableColumn[_, _]]
        val constraints = new ColumnConstraints
        constraints.setPercentWidth(col.getPercentWidth.get)

        val sp = new StackPane

        if (i == 0) { // Quick fix for not showing the horizontal scroll bar.
          val diff: NumberBinding = sp.widthProperty.subtract(5)
          column.prefWidthProperty.bind(diff)
        }
        else column.prefWidthProperty.bind(sp.widthProperty)

        arr1 += constraints
        arr2 += sp
        i += 1
      }

      grid.getColumnConstraints.addAll(arr1: _*)
      grid.addRow(0, arr2: _*)
    }
  })

  getChildren.addAll(grid, table)

  def getTreeTableView: TreeTableView[s] = this.table
}
