import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

class MicroSimulation extends Simulation {

  private val baseUrl = System.getenv().getOrDefault("BASE_URL", "http://localhost:30091")
  println(s"Using $baseUrl as Base URL.")
  val httpProtocolStandalone: HttpProtocolBuilder = http
//    .shareConnections
    .baseUrl(baseUrl)
    .acceptHeader("application/json,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val thingCreationRequest: String = """{"attributes" : { "name" : "Hello Thing", "temp" : 42} }"""
  val closedModelTrafficProfile = Seq(
    rampConcurrentUsers(1) to (4) during (4 seconds), // warm up the cluster, just a "kick-start"
    rampConcurrentUsers(4) to (8) during (4 seconds), // Switch to first gear
    constantConcurrentUsers(16) during (4 seconds), // Drive steady at first gear
    rampConcurrentUsers(16) to (32) during (4 seconds), // Switch to second gear
    constantConcurrentUsers(32) during (30 minutes), // Drive steady at second gear
  )

  val decentLoadScenario: ScenarioBuilder = scenario("Decent Load Scenario")
    .exec(http("Create Thing..")
      .post("/api/thing")
      .body(StringBody(thingCreationRequest)).asJson
      .check(status.is(201))
      .check(jsonPath("$.thingID").exists))

  setUp(decentLoadScenario.inject(closedModelTrafficProfile).protocols(httpProtocolStandalone))
}
