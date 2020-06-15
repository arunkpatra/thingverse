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
