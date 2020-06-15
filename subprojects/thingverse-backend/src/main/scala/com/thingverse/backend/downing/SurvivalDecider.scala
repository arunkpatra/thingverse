package com.thingverse.backend.downing

import akka.actor.Address
import akka.cluster.{Member, UniqueAddress}
import com.thingverse.backend.downing.SurvivalDecider.ClusterState
import com.typesafe.config.Config

import scala.collection.immutable.SortedSet
import scala.collection.{JavaConverters, Set}

trait SurvivalDecider {
  def isInMinority(clusterState: ClusterState, selfAddress: Address): Boolean
}

object SurvivalDecider {

  private val memberOrdering = new Ordering[ClusterMemberInfo] {
    override def compare(x: ClusterMemberInfo, y: ClusterMemberInfo): Int =
      Member.addressOrdering.compare(x.uniqueAddress.address, y.uniqueAddress.address)
  }

  def apply(config: Config): SurvivalDecider = {
    val cc = config.getConfig("thingverse-akka-downing")

    cc.getString("active-strategy") match {
      case "static-quorum" =>
        val ccc = cc.getConfig("static-quorum")
        val quorumSize = ccc.getInt("quorum-size")
        new FixedQuorumDecider(quorumSize, getRole(ccc))

      case "keep-majority" =>
        val ccc = cc.getConfig("keep-majority")
        new KeepMajorityDecider(getRole(ccc))

      case "keep-oldest" =>
        val ccc = cc.getConfig("keep-oldest")
        val downIfAlone = ccc.getBoolean("down-if-alone")
        new KeepOldestDecider(downIfAlone)

      case "role-based-static-quorum" =>
        val ccc = cc.getConfigList("role-based-static-quorum")
        val conditions = JavaConverters.asScalaIteratorConverter(ccc.iterator).asScala.toSeq.map(new RoleQuorum(_))
          .map(rq => (rq.quorumSize, rq.role))
        new RoleBasedFixedQuorumDecider(conditions)
    }
  }

  def getRole(c: Config): Option[String] = {
    c.getString("role") match {
      case r if r.trim.isEmpty => None
      case r => Some(r)
    }
  }

  def getRelevantMembers(clusterState: ClusterState, role: Option[String]): Set[ClusterMemberInfo] = {
    role match {
      case Some(r) => clusterState.upMembers.filter(_.roles contains r)
      case None => clusterState.upMembers
    }
  }

  class RoleQuorum(private val c: Config) {
    val quorumSize: Int = c.getInt("quorum-size")
    val role: Option[String] = getRole(c)
  }

  case class ClusterMemberInfo(uniqueAddress: UniqueAddress, roles: Set[String], member: Member)

  case class ClusterState(upMembers: Set[ClusterMemberInfo], unreachable: Set[UniqueAddress]) {
    lazy val sortedUpMembers: Set[ClusterMemberInfo] = SortedSet.empty(memberOrdering) ++ upMembers
    lazy val sortedUpAndReachable: Set[ClusterMemberInfo] =
      sortedUpMembers.filterNot(x => unreachable.contains(x.uniqueAddress))
    lazy val upReachable: Set[ClusterMemberInfo] = upMembers.filterNot(x => unreachable(x.uniqueAddress))
    lazy val upUnreachable: Set[ClusterMemberInfo] = upMembers.filter(x => unreachable(x.uniqueAddress))
  }

  class FixedQuorumDecider(quorumSize: Int, role: Option[String]) extends SurvivalDecider {
    override def isInMinority(clusterState: ClusterState, selfAddress: Address): Boolean = {
      (getRelevantMembers(clusterState, role) -- clusterState.upUnreachable).size < quorumSize
    }
  }

  class RoleBasedFixedQuorumDecider(conditions: Seq[(Int, Option[String])]) extends SurvivalDecider {
    override def isInMinority(clusterState: ClusterState, selfAddress: Address): Boolean = {
      val minorityDecision = conditions.map(c => isRoleInMinority(clusterState, c._1, c._2))
        .foldLeft(false)(_ || _)
      minorityDecision
    }

    private def isRoleInMinority(clusterState: ClusterState, quorumSize: Int, role: Option[String]): Boolean = {
      (getRelevantMembers(clusterState, role) -- clusterState.upUnreachable).size < quorumSize
    }
  }

  class KeepMajorityDecider(role: Option[String]) extends SurvivalDecider {
    override def isInMinority(clusterState: ClusterState, selfAddress: Address): Boolean = {
      role match {
        case Some(r) =>
          val all = clusterState.upMembers.filter(_.roles contains r)
          val unreachable = clusterState.upUnreachable.filter(_.roles contains r)
          all.size <= 2 * unreachable.size
        case None =>
          clusterState.upMembers.size <= 2 * clusterState.upUnreachable.size
      }
    }
  }

  class KeepOldestDecider(downIfAlone: Boolean) extends SurvivalDecider {
    override def isInMinority(clusterState: ClusterState, selfAddress: Address): Boolean = {
      val allRelevant = clusterState.upMembers
      val oldestRelevant = allRelevant.foldLeft(allRelevant.head)((a, b) => if (a.member isOlderThan b.member) a else b)

      if (downIfAlone) {
        clusterState.upReachable match {
          case s if s == Set(oldestRelevant) => true // only the oldest node --> terminate
          case _ if clusterState.unreachable == Set(oldestRelevant.uniqueAddress) => false // the oldest node is the only unreachable node --> survive
          case _ => clusterState.unreachable contains oldestRelevant.uniqueAddress
        }
      }
      else {
        clusterState.unreachable contains oldestRelevant.uniqueAddress
      }
    }
  }

}