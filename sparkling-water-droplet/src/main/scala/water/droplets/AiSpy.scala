
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
import scala.io.Source

object AiSpy {

  def main(args: Array[String]) {

    // I should calculate this many predictions:
    val pcount = 252 * 2
    // I should train from this many observations:
    val train_count = 252 * 20 // 20 years

    val mdt = new MutableDateTime()

    // Create Spark Context
    val conf = configure("Sparkling Water Droplet")
    val sc = new SparkContext(conf)

    // Create H2O Context
    val h2oContext = new H2OContext(sc).start()
    import h2oContext._

    // I should get all observations out of CSV
    val all_obs_s = "/home/ann/aispy/swd/data/ftr_ff_GSPC.csv"
    val all_obs_f = scala.io.Source.fromFile(all_obs_s)
    val all_obs   = all_obs_f.getLines
    val all_obs_a = all_obs.toArray

    // I should build a prediction loop from pcount.
    // I should have this number of days between training data and oos data:
    val train_oos_gap = 2
    var predictions_a = new Array[String](pcount)
    (1 to pcount).foreach(oos_i =>{
      var train_start = oos_i+train_oos_gap
      var train_end   = train_start+train_count
      // I should write oos-data to CSV:
      var oosf_s         = "/tmp/oos.csv"
      var oos_csv_writer = new PrintWriter(new File(oosf_s))
      oos_csv_writer.write(all_obs_a(0)    +"\n")
      oos_csv_writer.write(all_obs_a(oos_i)+"\n")
      oos_csv_writer.close
      // I should write train-data to CSV:
      var trainf_s         = "/tmp/train.csv"
      var train_csv_writer = new PrintWriter(new File(trainf_s))
      train_csv_writer.write(all_obs_a(0)    +"\n")
      var train_a = all_obs_a.slice(train_start,train_end)
      train_a.foreach(elm => train_csv_writer.write(elm+"\n"))
      train_csv_writer.close
      // I should build DataFrames from CSV files:
      var oos_df   = new DataFrame(new File(oosf_s  ))
      var train_df = new DataFrame(new File(trainf_s))
      // I should train
      var dlParams = new DeepLearningParameters()
      dlParams._train           = train_df('pctlead,'pctlag1,'pctlag2,'pctlag4,'pctlag8,'ip,'presult,'p2)
      dlParams._response_column = 'pctlead
      dlParams._epochs          = 20
      dlParams._activation      = Activation.RectifierWithDropout
      dlParams._hidden          = Array[Int](7,14)
      var dl                    = new DeepLearning(dlParams)
      var dlModel               = dl.trainModel.get
      // I should predict
      var dl_prediction_df = dlModel.score(oos_df)('predict)
      // I should prepare for reporting
      var dl_prediction_f  = dl_prediction_df.vec(0).at(0)
      var utime_l          = oos_df('cdate).vec(  0).at(0).toLong
      mdt.setMillis(utime_l)
      // Date formating should be refactored from 4 lines to 1 line
      var yr      = mdt.getYear.toString
      var moy     = f"${mdt.getMonthOfYear}%02d"
      var dom     = f"${mdt.getDayOfMonth}%02d"
      var date_s  = yr+"-"+moy+"-"+dom
      var cp_s    = oos_df('cp).vec(0).at(0)
      var pctlead_s          = oos_df('pctlead).vec(0).at(0)
      predictions_a(oos_i-1) = date_s+","+cp_s+","+dl_prediction_f+","+pctlead_s
      // I should manually garbage collect
      "/home/ann/aispy/curl_remove_all.bash" ! scala.sys.process.ProcessLogger(ln => println(ln))
      println(oos_i)})
    var pred_csv_writer = new PrintWriter(new File("/tmp/dl_pred.csv"))
    pred_csv_writer.write("cdate,cp,prediction,actual\n")
    predictions_a.foreach(elm => pred_csv_writer.write(elm+"\n"))
    pred_csv_writer.close
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
