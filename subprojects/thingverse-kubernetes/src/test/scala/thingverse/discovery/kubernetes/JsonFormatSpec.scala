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

package thingverse.discovery.kubernetes

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json._
import thingverse.discovery.kubernetes.svc.ServiceList.{Metadata, Service, ServicePort, ServiceSpec}
import thingverse.discovery.kubernetes.svc.{JsonFormat, ServiceList}

import scala.io.Source

class JsonFormatSpec extends AnyWordSpec with Matchers {
  "JsonFormat" should {
    val data = resourceAsString("services.json")

    "work" in {
      JsonFormat.serviceListFormat.read(data.parseJson) shouldBe ServiceList(
        List(
          Service(
            Some(ServiceSpec(
              "None",
              Some(List(
                ServicePort(Some("intra"), 7000),
                ServicePort(Some("tls"), 7001)))
            )),
            Some(Metadata("cassandra", deletionTimestamp = None))
          ),
          Service(
            Some(ServiceSpec(
              "10.105.215.34",
              Some(List(
                ServicePort(Some("http"), 8080)))
            )),
            Some(Metadata("thingverse-backend", deletionTimestamp = None))
          )
        ))
    }
  }

  private def resourceAsString(name: String): String =
    Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(name)).mkString
}
