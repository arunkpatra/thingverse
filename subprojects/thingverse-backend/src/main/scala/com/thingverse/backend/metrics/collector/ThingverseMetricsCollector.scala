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
