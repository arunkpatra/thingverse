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
