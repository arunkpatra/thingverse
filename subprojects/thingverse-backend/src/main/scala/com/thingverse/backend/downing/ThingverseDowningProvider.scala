package com.thingverse.backend.downing

import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable, Props}
import akka.cluster.ClusterEvent.{ClusterShuttingDown, _}
import akka.cluster.{Cluster, DowningProvider, MemberStatus}
import com.thingverse.backend.downing.SurvivalDecider.{ClusterMemberInfo, ClusterState}
import org.slf4j.LoggerFactory

import scala.concurrent.duration.{Duration, FiniteDuration}

class ThingverseDowningProvider(system: ActorSystem) extends DowningProvider {

  private val LOGGER = LoggerFactory.getLogger(classOf[ThingverseDowningProvider])

  import Helpers._

  private val cc = system.settings.config.getConfig("thingverse-akka-downing")

  override def downRemovalMargin: FiniteDuration = {
    val key = "down-removal-margin"
    toRootLowerCase(cc.getString(key)) match {
      case "off" => Duration.Zero
      case _ => cc.getMillisDuration(key) requiring(_ >= Duration.Zero, key + " > 0s, or off")
    }
  }

  override def downingActorProps: Option[Props] = {
    import Helpers._
    val cc = system.settings.config.getConfig("thingverse-akka-downing")
    val key = "stable-after"
    val stableInterval = cc.getMillisDuration(key) requiring(_ > Duration.Zero, key + " > 0s")

    val decider = SurvivalDecider(system.settings.config)
    LOGGER.info("Using SurvivalDecider of type {}.", decider.getClass.getName)

    Some(Props(new DowningActor(stableInterval, decider)))
  }
}

private[downing] class DowningActor(stableInterval: FiniteDuration, decider: SurvivalDecider) extends Actor with ActorLogging {

  private val cluster = Cluster.get(context.system)
  private var state: ClusterState = _
  private var unreachableTimer = Option.empty[Cancellable]
  cluster.subscribe(self, classOf[ClusterDomainEvent])

  override def receive: Receive = {
    // cluster.state may or may not reflect the changes during event handling, so we need to keep track of cluster
    // state ourselves
    case ClusterShuttingDown =>
      log.info("This cluster node is shutting down. Goodbye.")
    case CurrentClusterState(members, unreachable, _, _, _) =>
      val upMembers = members.filter(_.status == MemberStatus.Up).map(m => ClusterMemberInfo(m.uniqueAddress, m.roles, m))
      state = ClusterState(upMembers, unreachable.map(_.uniqueAddress))
      triggerTimer()
    case MemberUp(m) =>
      //TODO is there a good and meaningful way to count 'weaklyUp' members?
      state = state.copy(upMembers = state.upMembers + ClusterMemberInfo(m.uniqueAddress, m.roles, m))
      triggerTimer()
    case MemberLeft(m) =>
      state = state.copy(upMembers = state.upMembers.filterNot(_.uniqueAddress == m.uniqueAddress))
      triggerTimer()
    case ReachableMember(m) =>
      state = state.copy(unreachable = state.unreachable - m.uniqueAddress)
      triggerTimer()
    case UnreachableMember(m) =>
      //      println ("******* UNREACHABLE: " + m.uniqueAddress.address.port)
      state = state.copy(unreachable = state.unreachable + m.uniqueAddress)
      triggerTimer()
    case LeaderChanged(leader) =>
      log.info("Leader has changed. Leader address was {}", leader.get.toString)
    //    case RoleLeaderChanged(role, leader) =>
    //      log.info("Leader role changed. Leader address was {}, role was {} ", leader.get.toString, role)
    //    case SeenChanged(convergence, seenBy) =>
    //      log.info("Change was noticed.")
    case SplitBrainDetected(clusterState) if decider.isInMinority(clusterState, cluster.selfAddress) =>
      //      println ("########################### " + clusterState.upMembers + " |||||||||||||||||||| " + clusterState.unreachable + " >>>>>>>>>>>>>>>>>>> " + clusterState.upUnreachable)
      log.error("Network partition detected. I am not in the surviving partition --> terminating")
      context.system.terminate()
      context.become(Actor.emptyBehavior)
    case SplitBrainDetected(clusterState) if iAmResponsibleAction(clusterState) =>
      //      println ("########################### " + clusterState.upMembers + " |||||||||||||||||||| " + clusterState.unreachable + " >>>>>>>>>>>>>>>>>>> " + clusterState.upUnreachable)
      log.warning("Network partition detected. I am the responsible node in the surviving partition --> terminating unreachable nodes {}", cluster.state.unreachable)
      cluster.state.unreachable.foreach(m => cluster.down(m.address))
    case SplitBrainDetected(clusterState) =>
      //      println ("########################### " + clusterState.upMembers + " |||||||||||||||||||| " + clusterState.unreachable + " >>>>>>>>>>>>>>>>>>> " + clusterState.upUnreachable)
      log.info("Network partition detected. I am in the surviving partition, but I am not the responsible node, so nothing needs to be done")
    case _ =>

  }

  private def triggerTimer(): Unit = {
    unreachableTimer.foreach(_.cancel())
    unreachableTimer = None

    if (state.unreachable.nonEmpty) {
      import context.dispatcher
      // Store the cluster's state in the message to ensure split brain detection is done based on the state that was stable.
      //  If the handler reads the then-current cluster state, that may have changed between the scheduler firing and the event
      //  being handled
      unreachableTimer = Some(context.system.scheduler.scheduleOnce(stableInterval, self, SplitBrainDetected(state)))
    }
  }

  private def iAmResponsibleAction(clusterState: ClusterState): Boolean = {
    clusterState.sortedUpAndReachable.head.uniqueAddress.address == cluster.selfAddress
  }

  override def postStop(): Unit = {
    super.postStop()
    unreachableTimer.foreach(_.cancel())
  }

  case class SplitBrainDetected(clusterState: ClusterState)

}