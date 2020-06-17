package thingverse.discovery.kubernetes.svc

import akka.annotation.InternalApi

import scala.collection.immutable

/**
 * INTERNAL API
 */
@InternalApi private[thingverse] object ServiceList {

  final case class Metadata(name: String, deletionTimestamp: Option[String])

  final case class ServicePort(name: Option[String], port: Int)

  final case class ServiceSpec(clusterIP: String, ports: Option[immutable.Seq[ServicePort]])

  final case class Service(spec: Option[ServiceSpec], metadata: Option[Metadata])

}

final case class ServiceList(items: immutable.Seq[ServiceList.Service])
