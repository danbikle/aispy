/*
Spencer,
Your tips are useful!
I was able to operate deepSlice() using one of your ideas.
I had trouble passing a Vector of Booleans to mask rows.
IntelliJ tells me this when I try that idea:
scala.collection.immutable.Vector cannot be cast to water.fvec.Frame

Question:
How to cast Vector to Frame?

Dan
*/

/*
This script works with this commit:
commit def9d0093
Author: mmalohlava
Date:   Fri Feb 27 18:37:49 2015 -0800
*/

package water.droplets

import java.io.File
import org.apache.spark.h2o.{StringHolder, H2OContext}
import org.apache.spark.{SparkFiles, SparkContext, SparkConf}
import water.fvec._

object DeepSliceDemo {

  def main(args: Array[String]) {

    // Create Spark Context
    val conf = configure("Sparkling Water Droplet")
    val sc = new SparkContext(conf)

    // Create H2O Context
    val h2oContext = new H2OContext(sc).start()
    import h2oContext._

    // Register file to be available on all nodes
    sc.addFile("data/iris.csv")

    // Load data and parse it via h2o parser
    val irisTable = new DataFrame(new File(SparkFiles.get("iris.csv")))

    // Demo deepSlice()
    // null,null gives me all rows, all columns:
    val dsdf1   = irisTable.deepSlice(null,null)
    // deepSlice can take long[] for both args:
    val rows_a  = Array[Long](3)
    val cols_a  = Array[Long](0)
    val dsdf2   = irisTable.deepSlice(rows_a,cols_a)
    val cell03  = irisTable.vec(0).at(3)
    val cellx   = dsdf2.vec(0).at(0)
    // dsdf2.vec(0).at(0) should == cell03
    // Alternatively, you can produce more complicated row slice with 
    // boolean Vec (1 for in, 0 for out) and pass that in for orows.
    val rowcount = irisTable.numRows().toInt
    var bool_a   = (1 to rowcount).map(row => false).toArray
    bool_a(0)    = true
    bool_a(1)    = true
    bool_a(2)    = true
    bool_a(3)    = true
    val bool_v   = bool_a.toVector
    // val dsdf3    = irisTable.deepSlice(bool_v,cols_a)
    // Above call gives exception:
    // scala.collection.immutable.Vector cannot be cast to water.fvec.Frame
    // dsdf3.numRows() should == 4

    // Shutdown application
    sc.stop()
  }

  def configure(appName:String = "DeepSliceDemo"):SparkConf = {
    val conf = new SparkConf()
      .setAppName(appName)
    conf.setIfMissing("spark.master", sys.env.getOrElse("spark.master", "local"))
    conf
  }
}
