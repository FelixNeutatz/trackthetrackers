/**
 * Track the trackers
 * Copyright (C) 2015  Sebastian Schelter, Felix Neutatz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.ssc.trackthetrackers.analysis.statistics

import io.ssc.trackthetrackers.Config
import io.ssc.trackthetrackers.analysis.{FlinkUtils, GraphUtils}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.FileSystem.WriteMode

object CompanyDistribution extends App {

  computeDistribution(Config.get("analysis.trackingraphsample.path"), Config.get("webdatacommons.pldfile.unzipped"),
    Config.get("analysis.results.path") + "companyDistribution")

  def computeDistribution(trackingGraphFile: String, domainIndexFile: String, outputPath: String) = {

    implicit val env = ExecutionEnvironment.getExecutionEnvironment

    val edges = GraphUtils.readEdges(trackingGraphFile)
    val domains = GraphUtils.readVertices(domainIndexFile)

    val numTrackedHosts = edges.distinct("target").map { _ => Tuple1(1L) }.sum(0)

    val companyEdges = edges.filter { edge => Dataset.domainsByCompany.contains(edge.src.toInt) }
                            .map { edge => Dataset.domainsByCompany(edge.src.toInt) -> edge.target }
                            .distinct

    val companyCounts = FlinkUtils.countByStrKey(companyEdges, { t: (String, Int) => t._1 })

    val companyProbabilities = companyCounts.map(new CompanyProbability())
                                            .withBroadcastSet(numTrackedHosts, "numTrackedHosts")

    companyProbabilities.writeAsCsv(outputPath, fieldDelimiter = "\t", writeMode = WriteMode.OVERWRITE)
    env.execute()
  }

  class CompanyProbability() extends RichMapFunction[(String, Long), (String, Double)] {

    override def map(companyWithCount: (String, Long)): (String, Double) = {

      val numTrackedHosts = getRuntimeContext.getBroadcastVariable[Tuple1[Long]]("numTrackedHosts").get(0)._1

      companyWithCount._1 -> companyWithCount._2.toDouble / numTrackedHosts
    }
  }

}
