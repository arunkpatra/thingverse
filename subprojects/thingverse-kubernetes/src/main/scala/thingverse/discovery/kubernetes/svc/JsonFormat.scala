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

import akka.annotation.InternalApi
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import thingverse.discovery.kubernetes.svc.ServiceList._

/**
 * INTERNAL API
 */
@InternalApi private[thingverse] object JsonFormat extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val containerPortFormat: JsonFormat[ServicePort] = jsonFormat2(ServicePort)
  implicit val metadataFormat: JsonFormat[Metadata] = jsonFormat2(Metadata)
  implicit val serviceSpecFormat: JsonFormat[ServiceSpec] = jsonFormat2(ServiceSpec)
  implicit val serviceFormat: JsonFormat[Service] = jsonFormat2(Service)
  implicit val serviceListFormat: RootJsonFormat[ServiceList] = jsonFormat1(ServiceList.apply)
}
