
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
import org.joda.time.MutableDateTime
import sys.process._

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

    val mdt = new MutableDateTime()
    val gbm_csv_writer = new PrintWriter(new File("/tmp/gbm_predictions.csv"))
    val dl_csv_writer  = new PrintWriter(new File("/tmp/dl_predictions.csv" ))

    // Calculate predictions
    val gbm_predictions_df = gbmModel.score(oos2_df)('predict)
    val dl_predictions_df  = dlModel.score( oos2_df)('predict)
    val numRows            = dl_predictions_df.numRows().toInt
    (0 to numRows-1).foreach(rnum => {
      var gbm_prediction_f = gbm_predictions_df.vec(0).at(rnum)
      var dl_prediction_f  = dl_predictions_df.vec( 0).at(rnum)
      var utime_l          = oos_df('cdate).vec(    0).at(rnum).toLong
      mdt.setMillis(utime_l)
      // Date formating should be refactored from 4 lines to 1 line
      var yr               = mdt.getYear.toString
      var moy              = f"${mdt.getMonthOfYear}%02d"
      var dom              = f"${mdt.getDayOfMonth}%02d"
      var date_s           = yr+"-"+moy+"-"+dom
      var cp_f             = oos_df('cp).vec(     0).at(rnum)
      var actual_f         = oos_df('pctlead).vec(0).at(rnum)
      var gbm_csv_s = date_s+","+cp_f+","+gbm_prediction_f+","+actual_f+"\n"
      var dl_csv_s  = date_s+","+cp_f+","+dl_prediction_f+ ","+actual_f+"\n"
      gbm_csv_writer.write(gbm_csv_s)
      dl_csv_writer.write(  dl_csv_s)
      println("oos row processed: "+rnum)})
    gbm_csv_writer.close
    dl_csv_writer.close

    // I should manually garbage collect
    "/home/ann/aispy/curl_remove_all.bash" ! scala.sys.process.ProcessLogger(ln => println(ln))

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
