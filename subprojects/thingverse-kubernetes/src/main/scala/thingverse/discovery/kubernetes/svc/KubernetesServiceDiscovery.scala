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

package thingverse.discovery.kubernetes.svc

import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.annotation.InternalApi
import akka.discovery.ServiceDiscovery.{Resolved, ResolvedTarget}
import akka.discovery._
import akka.event.Logging
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import com.typesafe.sslconfig.ssl.TrustStoreConfig
import thingverse.discovery.kubernetes.svc.JsonFormat._

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.Try
import scala.util.control.{NoStackTrace, NonFatal}

object KubernetesServiceDiscovery {

  /**
   * INTERNAL API
   *
   * Finds relevant targets given a service list. Note that this doesn't filter by name as it is the job of the selector
   * to do that.
   */
  @InternalApi
  private def targets(
                       serviceList: ServiceList,
                       portName: Option[String],
                       serviceNamespace: String,
                       serviceDomain: String,
                       rawIp: Boolean): Seq[ResolvedTarget] =

    for {
      item <- serviceList.items
      // Maybe port is an Option of a port, and will be None if no portName was requested
      maybePort <- portName match {
        case None =>
          Seq(None)
        case Some(name) =>
          for {
            ports <- item.spec.get.ports.seq
            port <- ports
            if port.name.contains(name)
          } yield Some(port.port)
      }
    } yield {
      val ip = item.spec.get.clusterIP
      val serviceName = item.metadata.get.name
      //val hostOrIp = if (rawIp) ip else s"${ip.replace('.', '-')}.$serviceNamespace.svc.$serviceDomain"
      val hostOrIp = if (rawIp) ip else s"$serviceName.$serviceNamespace.svc.$serviceDomain"
      val target = ResolvedTarget(
        host = hostOrIp,
        port = maybePort,
        address = None
        //              address = Some(InetAddress.getByName(ip))
      )
      target
    }

  class KubernetesApiException(msg: String) extends RuntimeException(msg) with NoStackTrace

}

/**
 * An alternative implementation that uses the Kubernetes API. The main advantage of this method is that it allows
 * you to define readiness/health checks that don't affect the bootstrap mechanism.
 */
class KubernetesServiceDiscovery(system: ActorSystem) extends ServiceDiscovery {

  import KubernetesServiceDiscovery._
  import system.dispatcher

  private val http = Http()(system)

  private val settings = Settings(system)

  private implicit val mat: Materializer = Materializer(system)

  private val log = Logging(system, getClass)

  private val httpsTrustStoreConfig =
    TrustStoreConfig(data = None, filePath = Some(settings.apiCaPath)).withStoreType("PEM")

  private val httpsConfig =
    AkkaSSLConfig()(system).mapSettings(s =>
      s.withTrustManagerConfig(s.trustManagerConfig.withTrustStoreConfigs(Seq(httpsTrustStoreConfig))))

  private val httpsContext = http.createClientHttpsContext(httpsConfig)

  log.debug("Settings {}", settings)
  private val apiToken = readConfigVarFromFilesystem(settings.apiTokenPath, "api-token").getOrElse("")
  private val serviceNamespace = settings.serviceNamespace
    .orElse(readConfigVarFromFilesystem(settings.serviceNamespacePath, "service-namespace"))
    .getOrElse("default")

  override def lookup(query: Lookup, resolveTimeout: FiniteDuration): Future[Resolved] = {
    val labelSelector = settings.serviceLabelSelector(query.serviceName)

    log.info(
      "Querying for services with label selector: [{}]. Namespace: [{}]. Port: [{}]",
      labelSelector,
      serviceNamespace,
      query.portName)

    for {
      request <- optionToFuture(
        serviceRequest(apiToken, serviceNamespace, labelSelector),
        s"Unable to form request; check Kubernetes environment (expecting env vars ${settings.apiServiceHostEnvName}, ${settings.apiServicePortEnvName})"
      )

      response <- http.singleRequest(request, httpsContext)

      entity <- response.entity.toStrict(resolveTimeout)

      serviceList <- {

        response.status match {
          case StatusCodes.OK =>
            log.debug("Kubernetes API entity: [{}]", entity.data.utf8String)
            val unmarshalled = Unmarshal(entity).to[ServiceList]
            unmarshalled.failed.foreach { t =>
              log.warning(
                "Failed to unmarshal Kubernetes API response.  Status code: [{}]; Response body: [{}]. Ex: [{}]",
                response.status.value,
                entity,
                t.getMessage)
            }
            unmarshalled
          case StatusCodes.Forbidden =>
            Unmarshal(entity).to[String].foreach { body =>
              log.warning(
                "Forbidden to communicate with Kubernetes API server; check RBAC settings. Response: [{}]",
                body)
            }
            Future.failed(
              new KubernetesApiException("Forbidden when communicating with the Kubernetes API. Check RBAC settings."))
          case other =>
            Unmarshal(entity).to[String].foreach { body =>
              log.warning(
                "Non-200 when communicating with Kubernetes API server. Status code: [{}]. Response body: [{}]",
                other,
                body
              )
            }

            Future.failed(new KubernetesApiException(s"Non-200 from Kubernetes API server: $other"))
        }

      }

    } yield {
      val addresses = targets(serviceList, query.portName, serviceNamespace, settings.serviceDomain, settings.rawIp)
      if (addresses.isEmpty && serviceList.items.nonEmpty) {
        if (log.isInfoEnabled) {
          val servicePortNames = serviceList.items.flatMap(_.spec).flatMap(_.ports).flatten.toSet
          log.info(
            "No targets found from service list. Is the correct port name configured? Current configuration: [{}]. Ports on service: [{}]",
            query.portName,
            servicePortNames
          )
        }
      } else {
        log.info("Resolved these targets for serviceName {}: [{}]", query.serviceName, addresses)
      }
      Resolved(
        serviceName = query.serviceName,
        addresses = addresses
      )
    }
  }

  private def optionToFuture[T](option: Option[T], failMsg: String): Future[T] =
    option.fold(Future.failed[T](new NoSuchElementException(failMsg)))(Future.successful)

  private def serviceRequest(token: String, namespace: String, labelSelector: String) =
    for {
      host <- sys.env.get(settings.apiServiceHostEnvName)
      portStr <- sys.env.get(settings.apiServicePortEnvName)
      port <- Try(portStr.toInt).toOption
    } yield {
      val path = Uri.Path.Empty / "api" / "v1" / "namespaces" / namespace / "services"
      val query = Uri.Query("labelSelector" -> labelSelector)
      val uri = Uri.from(scheme = "https", host = host, port = port).withPath(path).withQuery(query)

      HttpRequest(uri = uri, headers = Seq(Authorization(OAuth2BearerToken(token))))
    }

  /**
   * This uses blocking IO, and so should only be used to read configuration at startup.
   */
  private def readConfigVarFromFilesystem(path: String, name: String): Option[String] = {
    val file = Paths.get(path)
    if (Files.exists(file)) {
      try {
        Some(new String(Files.readAllBytes(file), "utf-8"))
      } catch {
        case NonFatal(e) =>
          log.error(e, "Error reading {} from {}", name, path)
          None
      }
    } else {
      log.warning("Unable to read {} from {} because it doesn't exist.", name, path)
      None
    }
  }
}
