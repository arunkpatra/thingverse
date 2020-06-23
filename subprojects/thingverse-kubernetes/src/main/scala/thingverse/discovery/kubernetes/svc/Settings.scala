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

  val thingverseKubernetesApi: Config = system.settings.config.getConfig("akka.discovery.kubernetes-service")

  val rawIp: Boolean = thingverseKubernetesApi.getBoolean("use-raw-ip")

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
