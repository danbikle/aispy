
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
    val pcount = 22
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
    // Higher dofit means fewer models means faster loop:
    val dofit         = 4
    // I should have this number of days between training data and oos data:
    val train_oos_gap = dofit + 1// train_oos_gap should <= dofit
    // I should get DeepLearning ready:
    var dlParams      = new DeepLearningParameters()
    var dl            = new DeepLearning(dlParams)
    // I should jump through hoops to initialize dlModel:
    var trainf_s         = "/tmp/train.csv"
    var train_csv_writer = new PrintWriter(new File(trainf_s))
    train_csv_writer.write(all_obs_a(0)+"\n")
    var train_a = all_obs_a.slice(1,100)
    train_a.foreach(elm => train_csv_writer.write(elm+"\n"))
    train_csv_writer.close
    var train_df = new DataFrame(new File(trainf_s))
    dlParams._train           = train_df('pctlead,'pctlag1,'pctlag2,'pctlag4,'pctlag8,'ip,'presult,'p2)
    dlParams._response_column = 'pctlead
    dlParams._epochs          = 2
    dlParams._activation      = Activation.RectifierWithDropout
    dlParams._hidden          = Array[Int](7,14)
    dl                        = new DeepLearning(dlParams)
    // Now I can intialize dlModel:
    var dlModel       = dl.trainModel.get
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
      // var train_csv_writer = new PrintWriter(new File(trainf_s))
train_csv_writer = new PrintWriter(new File(trainf_s))
      // train_csv_writer.write(all_obs_a(0)    +"\n")
      // var train_a = all_obs_a.slice(train_start,train_end)
      // train_a.foreach(elm => train_csv_writer.write(elm+"\n"))
      // train_csv_writer.close
      // I should build DataFrames from CSV files:
      var oos_df   = new DataFrame(new File(oosf_s  ))
      if ((oos_i == 1) || (oos_i % dofit == 0)) {
        // var train_df = new DataFrame(new File(trainf_s))
        // I should train
        // Build DeepLearning model
        dlParams._train           = train_df('pctlead,'pctlag1,'pctlag2,'pctlag4,'pctlag8,'ip,'presult,'p2)
        dlParams._response_column = 'pctlead
        dlParams._epochs          = 2
        dlParams._activation      = Activation.RectifierWithDropout
        dlParams._hidden          = Array[Int](7,14)
        dl                        = new DeepLearning(dlParams)
        dlModel                   = dl.trainModel.get}
      // I should predict
/*
      var dl_prediction_df = dlModel.score(oos_df)('predict)
      // I should prepare for reporting
      var dl_prediction_f  = dl_prediction_df.vec(0).at(0)
      var utime_l          = oos_df('cdate).vec( 0).at(0).toLong
*/
      println(oos_i)})

/**
    val all_obs_df = new DataFrame(new File(all_obs_s))
    var predictions_array = new Array[String](pcount)

    // I should demo how to slice some columns and rows
    var mycolnum = all_obs_df.numCols()
    var hello    = all_obs_df.vec(0)

    (0 to pcount-1).foreach(rnum => {
      println("rnum is: "+rnum)
      // I should get row from all_obs_df
      var oos_df  = all_obs_df('cdate)
      var utime_l = oos_df('cdate).vec(0).at(rnum).toLong
      mdt.setMillis(utime_l)
      // Date formating should be refactored from 4 lines to 1 line
      var yr      = mdt.getYear.toString
      var moy     = f"${mdt.getMonthOfYear}%02d"
      var dom     = f"${mdt.getDayOfMonth}%02d"
      var date_s  = yr+"-"+moy+"-"+dom
      var cp_s    = all_obs_df('cp).vec(0).at(rnum)
      var pctlead_s = all_obs_df('pctlead).vec(0).at(rnum)
      predictions_array(rnum) = date_s+","+cp_s+","+pctlead_s+","
      "endloop"
})

    // Register file to be available on all nodes
    sc.addFile("data/ftr_ff_GSPC_train.csv")
    sc.addFile("data/ftr_ff_GSPC_oos.csv"  )

    // Load data and parse it via h2o parser
    val train_df  = new DataFrame(new File(SparkFiles.get("ftr_ff_GSPC_train.csv")))
    val oos1_df   = new DataFrame(new File(SparkFiles.get("ftr_ff_GSPC_oos.csv"  )))
    val train2_df = train_df('pctlead,'pctlag1,'pctlag2,'pctlag4,'pctlag8,'ip,'presult,'p2)
    val oos2_df   =   oos1_df('pctlead,'pctlag1,'pctlag2,'pctlag4,'pctlag8,'ip,'presult,'p2)

    // Build GBM model
    val gbmParams              = new GBMParameters()
    gbmParams._train           = train2_df
    gbmParams._response_column = 'pctlead
    gbmParams._ntrees          = 5
    val gbm                    = new GBM(gbmParams)
    val gbmModel               = gbm.trainModel.get

    // Build DeepLearning model
    val dlParams2              = new DeepLearningParameters()
    dlParams2._train           = train2_df
    dlParams2._response_column = 'pctlead
    dlParams2._epochs          = 20
    dlParams2._activation      = Activation.RectifierWithDropout
    dlParams2._hidden          = Array[Int](7,14)
    val dl2                    = new DeepLearning(dlParams2)
    val dlModel2               = dl2.trainModel.get


    val gbm_csv_writer = new PrintWriter(new File("/tmp/gbm_predictions.csv"))
    val dl_csv_writer  = new PrintWriter(new File("/tmp/dl_predictions.csv" ))

    // Calculate predictions
    val gbm_predictions_df = gbmModel.score(oos2_df)('predict)
    val dl_predictions_df  = dlModel2.score( oos2_df)('predict)
    val numRows            = dl_predictions_df.numRows().toInt
    (0 to numRows-1).foreach(rnum => {
      var gbm_prediction_f = gbm_predictions_df.vec(0).at(rnum)
      var dl_prediction_f  = dl_predictions_df.vec( 0).at(rnum)
      var utime_l          = oos1_df('cdate).vec(   0).at(rnum).toLong
      mdt.setMillis(utime_l)
      // Date formating should be refactored from 4 lines to 1 line
      var yr               = mdt.getYear.toString
      var moy              = f"${mdt.getMonthOfYear}%02d"
      var dom              = f"${mdt.getDayOfMonth}%02d"
      var date_s           = yr+"-"+moy+"-"+dom
      var cp_f             = oos1_df('cp).vec(     0).at(rnum)
      var actual_f         = oos1_df('pctlead).vec(0).at(rnum)
      var gbm_csv_s = date_s+","+cp_f+","+gbm_prediction_f+","+actual_f+"\n"
      var dl_csv_s  = date_s+","+cp_f+","+dl_prediction_f+ ","+actual_f+"\n"
      gbm_csv_writer.write(gbm_csv_s)
      dl_csv_writer.write(  dl_csv_s)
      println("oos row processed: "+rnum)})
    gbm_csv_writer.close
    dl_csv_writer.close
**/

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
