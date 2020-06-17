/*
 * Copyright (C) 2017-2020 Lightbend Inc. <https://www.lightbend.com>
 */

package thingverse.discovery.kubernetes.svc

import java.util.Optional

import akka.actor._
import com.typesafe.config.Config

import scala.compat.java8.OptionConverters._

final class Settings(system: ExtendedActorSystem) extends Extension {

  /**
   * Copied from AkkaManagementSettings, which we don't depend on.
   */
  private implicit class HasDefined(val config: Config) {
    def optDefinedValue(key: String): Option[String] =
      if (hasDefined(key)) Some(config.getString(key)) else None

    def hasDefined(key: String): Boolean =
      config.hasPath(key) &&
        config.getString(key).trim.nonEmpty &&
        config.getString(key) != s"<$key>"
  }

  lazy val rawIp: Boolean = thingverseKubernetesApi.getBoolean("use-raw-ip")
  val apiCaPath: String =
    thingverseKubernetesApi.getString("api-ca-path")

  val apiTokenPath: String =
    thingverseKubernetesApi.getString("api-token-path")

  val apiServiceHostEnvName: String =
    thingverseKubernetesApi.getString("api-service-host-env-name")

  val apiServicePortEnvName: String =
    thingverseKubernetesApi.getString("api-service-port-env-name")

  val serviceNamespacePath: String =
    thingverseKubernetesApi.getString("service-namespace-path")

  val serviceNamespace: Option[String] =
    thingverseKubernetesApi.optDefinedValue("service-namespace")
  val serviceDomain: String =
    thingverseKubernetesApi.getString("service-domain")
  private val thingverseKubernetesApi = system.settings.config.getConfig("akka.discovery.kubernetes-service")

  /** Java API */
  def getServiceNamespace: Optional[String] = serviceNamespace.asJava

  def serviceLabelSelector(name: String): String =
    thingverseKubernetesApi.getString("service-label-selector").format(name)

  override def toString =
    s"Settings($serviceNamespace, $serviceNamespace, $serviceDomain)"
}

object Settings extends ExtensionId[Settings] with ExtensionIdProvider {
  override def get(system: ActorSystem): Settings = super.get(system)

  override def lookup: Settings.type = Settings

  override def createExtension(system: ExtendedActorSystem): Settings = new Settings(system)
}
