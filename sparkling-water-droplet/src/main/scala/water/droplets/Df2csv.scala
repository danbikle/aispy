/*
Hi Dan,

Frame#toCSV call returns common java.io.InputStream.
You can write it to the file or any other output (console).

Here is schematic code how to use toCSV from Scala:
val fr = new DataFrame(new java.net.URI("/tmp/iris_wheader.csv"))
val is = fr.toCSV(true, false) // #1 param = print header, #2 = print numbers in HEX format
while (is.available()>0) print(is.read.toChar)

michal
*/

package water.droplets
import java.io._
import org.apache.spark.h2o._
import org.apache.spark._
import water.fvec._
object SparklingWaterDroplet {

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

    // #1 param = print header, #2 = print numbers in HEX format
    val is = irisTable.toCSV(true, false) 
    val csv_writer = new PrintWriter(new File("/tmp/myiris.csv"))
    while (is.available()>0) {
      csv_writer.write(is.read.toChar)}
    csv_writer.close

    // Shutdown application
    sc.stop()

}

  def configure(appName:String = "Sparkling Water Demo"):SparkConf = {
    val conf = new SparkConf()
      .setAppName(appName)
    conf.setIfMissing("spark.master", sys.env.getOrElse("spark.master", "local"))
    conf
  }
}

