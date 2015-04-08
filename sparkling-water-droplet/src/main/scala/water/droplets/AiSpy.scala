
package water.droplets

import java.io._

import hex.tree.gbm.GBM
import hex.tree.gbm.GBMModel.GBMParameters

import hex.deeplearning.DeepLearning
import hex.deeplearning.DeepLearningModel.DeepLearningParameters
import hex.deeplearning.DeepLearningModel.DeepLearningParameters.Activation

import org.apache.spark.h2o.{StringHolder, H2OContext}
import org.apache.spark.{SparkFiles, SparkContext, SparkConf}
import water.fvec._
import water.api._

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
    val train_df  = new DataFrame(new File(SparkFiles.get("ftr_ff_GSPC_train.csv")))
    val oos_df    = new DataFrame(new File(SparkFiles.get("ftr_ff_GSPC_oos.csv"  )))
    val train2_df = train_df('pctlead,'pctlag1,'pctlag2,'pctlag4,'pctlag8,'ip,'presult,'p2)
    val oos2_df   =   oos_df('pctlead,'pctlag1,'pctlag2,'pctlag4,'pctlag8,'ip,'presult,'p2)

    // Build GBM model
    val gbmParams              = new GBMParameters()
    gbmParams._train           = train2_df
    gbmParams._response_column = 'pctlead
    gbmParams._ntrees          = 5
    val gbm                    = new GBM(gbmParams)
    val gbmModel               = gbm.trainModel.get

    // Build DeepLearning model
    val dlParams              = new DeepLearningParameters()
    dlParams._train           = train2_df
    dlParams._response_column = 'pctlead
    dlParams._epochs          = 20
    dlParams._activation      = Activation.RectifierWithDropout
    dlParams._hidden          = Array[Int](7,14)
    val dl                    = new DeepLearning(dlParams)
    val dlModel               = dl.trainModel.get

    val csv_writer = new PrintWriter(new File("/tmp/dl_predictions.csv"))
    // Make prediction on train data
    val predictions_df = dlModel.score(oos2_df)('predict)
    val numRows        = predictions_df.numRows().toInt
    (0 to numRows-1).foreach(rnum => {
      var utime_i      = oos_df('cdate).vec(0).at(rnum)
      var cp_f         = oos_df('cp).vec(0).at(rnum)
      var prediction_f = predictions_df.vec(0).at(rnum)
      var actual_f     = oos_df('pctlead).vec(0).at(rnum)
      var csv_s        = utime_i+","+cp_f+","+prediction_f+","+actual_f+"\n"
      csv_writer.write(csv_s)
      print(csv_s)})
    csv_writer.close

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
