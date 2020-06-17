/*
 * Copyright (C) 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.thingverse.backend.metrics.listener

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.metrics.StandardMetrics.{Cpu, HeapMemory}
import akka.cluster.metrics.{ClusterMetricsChanged, ClusterMetricsExtension, NodeMetrics}
import com.thingverse.backend.metrics.collector.ThingverseStandardMetrics.ThingverseActorStats

//noinspection TypeAnnotation
class ThingverseClusterNodeMetricsListener extends Actor with ActorLogging {
  val selfAddress = Cluster(context.system).selfAddress
  val extension = ClusterMetricsExtension(context.system)

  log.info("[Metrics] Starting Thingverse Metrics Collector")

  // Subscribe unto ClusterMetricsEvent events.
  override def preStart(): Unit = extension.subscribe(self)

  // Unsubscribe from ClusterMetricsEvent events.
  override def postStop(): Unit = extension.unsubscribe(self)

  def receive = {
    case ClusterMetricsChanged(clusterMetrics) =>
      clusterMetrics.filter(_.address == selfAddress).foreach { nodeMetrics =>
        //  logThingverseStats(nodeMetrics)
        //  logHeap(nodeMetrics)
        //  logCpu(nodeMetrics)
      }
    case state: CurrentClusterState => // ignore
  }

  def logThingverseStats(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    //    case ThingverseActorStats(address, timestamp, errorCount, requestCount, thingCount, deadLetterMsgCount) =>
    //      log.info("[TS] Total things: {}", thingCount)
    case ThingverseActorStats(_, _, _, _, _, deadLetterMsgCount) =>
      log.info("[TS] Total dead letters: {}", deadLetterMsgCount)
    case _ => // no info

  }

  def logHeap(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    case HeapMemory(address, timestamp, used, committed, max) =>
      log.info("Used heap: {} MB", used.doubleValue / 1024 / 1024)
    case _ => // No heap info.
  }

  def logCpu(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    case Cpu(address, timestamp, Some(systemLoadAverage), cpuCombined, cpuStolen, processors) =>
      log.info("Load: {} ({} processors)", systemLoadAverage, processors)
    case _ => // No cpu info.
  }
}
