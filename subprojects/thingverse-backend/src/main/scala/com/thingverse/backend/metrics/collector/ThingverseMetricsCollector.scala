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

package com.thingverse.backend.metrics.collector

import akka.actor.{ActorSystem, Address}
import akka.cluster.Cluster
import akka.cluster.metrics._

object ThingverseStandardMetrics {

  final val DeadLetterMsgCount = "thingverse-dead-letter-count"
  final val TotalErrorCount = "thingverse-total-error-count"
  final val TotalRequestCount = "thingverse-total-request-count"
  final val TotalThingCount = "thingverse-total-thing-count"

  object ThingverseActorStats {

    /**
     * Given a NodeMetrics it returns the ThingverseActorStats data if the nodeMetrics contains
     * necessary Thingverse metrics.
     *
     * @return if possible a tuple matching the ThingverseActorStats constructor parameters
     */
    def unapply(nodeMetrics: NodeMetrics): Option[(Address, Long, Long, Long, Long, Long)] = {
      for {
        errorCount <- nodeMetrics.metric(TotalErrorCount)
        requestCount <- nodeMetrics.metric(TotalRequestCount)
        thingCount <- nodeMetrics.metric(TotalThingCount)
        deadLtrMsgCt <- nodeMetrics.metric(DeadLetterMsgCount)
      } yield (
        nodeMetrics.address,
        nodeMetrics.timestamp,
        errorCount.value.longValue,
        requestCount.value.longValue,
        thingCount.value.longValue,
        deadLtrMsgCt.value.longValue())
    }
  }
}

class ThingverseMetricsCollector(system: ActorSystem, address: Address, decayFactor: Double)
  extends JmxMetricsCollector(address, decayFactor) {

  import ThingverseStandardMetrics._

  private val decayFactorOption = Some(decayFactor)

  private def this(system: ActorSystem, address: Address, settings: ClusterMetricsSettings) =
    this(system: ActorSystem, address, EWMA.alpha(settings.CollectorMovingAverageHalfLife, settings.CollectorSampleInterval))

  /**
   * This constructor is used when creating an instance from configured FQCN
   */
  def this(system: ActorSystem) = this(system, Cluster(system).selfAddress, ClusterMetricsExtension(system).settings)

  override def metrics(): Set[Metric] = {
    super.metrics.union(Set(errorCount, requestCount, thingCount, deadLetterMsgCount).flatten)
  }

  /**
   * Verify at the end of construction that everything works just fine.
   */
  metrics()

  def deadLetterMsgCount: Option[Metric] = {
    Metric.create(name = DeadLetterMsgCount, value = system.mailboxes.deadLetterMailbox.numberOfMessages,
      decayFactor = None)
  }

  def errorCount: Option[Metric] = Metric.create(name = TotalErrorCount, value = 40, decayFactor = None)

  def requestCount: Option[Metric] = Metric.create(name = TotalRequestCount, value = 41, decayFactor = None)

  def thingCount: Option[Metric] = Metric.create(name = TotalThingCount, value = 42, decayFactor = None)
}
