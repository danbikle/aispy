
package water.droplets

import java.io.File

import hex.tree.gbm.GBM
import hex.tree.gbm.GBMModel.GBMParameters
import org.apache.spark.h2o.{StringHolder, H2OContext}
import org.apache.spark.{SparkFiles, SparkContext, SparkConf}
import water.fvec.DataFrame

object AiSpy {

  def main(args: Array[String]) {

    // Create Spark Context
    val conf = configure("Sparkling Water Droplet")
    val sc = new SparkContext(conf)

    // Create H2O Context
    val h2oContext = new H2OContext(sc).start()
    import h2oContext._

    // Register file to be available on all nodes
    sc.addFile("data/ftr_ff_GSPC_train.csv")
    sc.addFile("data/ftr_ff_GSPC_oos.csv"  )

    // Load data and parse it via h2o parser
    val train_df = new DataFrame(new File(SparkFiles.get("ftr_ff_GSPC_train.csv")))
    val oos_df   = new DataFrame(new File(SparkFiles.get("ftr_ff_GSPC_oos.csv"  )))

    // Shutdown application
    sc.stop()
  }

  def configure(appName:String = "AiSpy Demo"):SparkConf = {
    val conf = new SparkConf()
      .setAppName(appName)
    conf.setIfMissing("spark.master", sys.env.getOrElse("spark.master", "local"))
    conf
  }
}
