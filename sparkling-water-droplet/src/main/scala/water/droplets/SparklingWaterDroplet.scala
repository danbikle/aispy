/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package water.droplets

import java.io.File

import hex.tree.gbm.GBM
import hex.tree.gbm.GBMModel.GBMParameters
import org.apache.spark.h2o.{StringHolder, H2OContext}
import org.apache.spark.{SparkFiles, SparkContext, SparkConf}
import water.fvec.DataFrame

/**
 * Example of Sparkling Water based application.
 */
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
    // Demo deepSlice()
    // null,null gives me all rows, all columns:
    val dcdf1   = irisTable.deepSlice(null,null)
    // deepSlice can take long[] for both args
    var long1_a = Array[Long](0,1,2,3,4)
    var long2_a = 0 until 4 toArray
    var long3_a = long2_a.map(elm => elm.longValue)
    var mylong  = 3.longValue
    val dcdf2   = irisTable.deepSlice(long3_a,null)
    val rows_a  = Array[Long](0,3,12,33,51)
    val cols_a  = Array[Long](0,2,4)
    val dcdf3   = irisTable.deepSlice(rows_a,cols_a)

    // Build GBM model
    val gbmParams = new GBMParameters()
    gbmParams._train = irisTable
    gbmParams._response_column = 'class
    gbmParams._ntrees = 5

    val gbm = new GBM(gbmParams)
    val gbmModel = gbm.trainModel.get

    // Make prediction on train data
    val predict = gbmModel.score(irisTable)('predict)

    // Compute number of mispredictions with help of Spark API
    val trainRDD = asRDD[StringHolder](irisTable('class))
    val predictRDD = asRDD[StringHolder](predict)

    // Make sure that both RDDs has the same number of elements
    assert(trainRDD.count() == predictRDD.count)
    val numMispredictions = trainRDD.zip(predictRDD).filter( i => {
      val act = i._1
      val pred = i._2
      act.result != pred.result
    }).collect()

    println(
      s"""
         |Number of mispredictions: ${numMispredictions.length}
         |
         |Mispredictions:
         |
         |actual X predicted
         |------------------
         |${numMispredictions.map(i => i._1.result.get + " X " + i._2.result.get).mkString("\n")}
       """.stripMargin)

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
