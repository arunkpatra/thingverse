import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

// See https://medium.com/@vcomposieux/load-testing-gatling-tips-tricks-47e829e5d449

class BasicUnsecuredStandaloneSimulation extends Simulation {

  private val baseUrl = System.getenv().getOrDefault("BASE_URL", "http://localhost:30001")
  println(s"Using $baseUrl as Base URL.")
  //val tokenFeeder = csv("token.csv").random
  val httpProtocolStandalone: HttpProtocolBuilder = http
    .shareConnections
    // Here is the root for all relative URLs
    .baseUrl(baseUrl)
    // Here are the common headers
    .acceptHeader("application/json,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val thingCreationRequest: String = """{"attributes" : { "name" : "Hello Thing", "temp" : 42} }"""

  val closedModelTrafficProfile = Seq(
    rampConcurrentUsers(1) to (120) during (120 seconds)
  )

  val basicUnsecuredStandaloneScenario: ScenarioBuilder = scenario("Simple scenario for light weight testing.")
    .exec(http("Create a new Thing")
      .post("/api/thing")
      .body(StringBody(thingCreationRequest)).asJson
      .check(status.is(201))
      .check(jsonPath("$.thingID").exists))

  setUp(basicUnsecuredStandaloneScenario.inject(closedModelTrafficProfile).protocols(httpProtocolStandalone))
}
