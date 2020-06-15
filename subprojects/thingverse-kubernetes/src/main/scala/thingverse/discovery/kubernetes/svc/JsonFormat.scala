package thingverse.discovery.kubernetes.svc

import akka.annotation.InternalApi
import thingverse.discovery.kubernetes.svc.ServiceList._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

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
