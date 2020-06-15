import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

// See https://medium.com/@vcomposieux/load-testing-gatling-tips-tricks-47e829e5d449

class BasicSecuredClusteredSimulation extends Simulation {

  val httpProtocolClusteredViaZuulProxy: HttpProtocolBuilder = http
    .shareConnections
    // Here is the root for all relative URLs
    .baseUrl("http://localhost:8762/thingverse-api")
    // Here are the common headers
    .acceptHeader("application/json,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val authenticationRequest: String = """{"username" : "dummy_user", "password" : "password"}"""
  val thingCreationRequest: String = """{"attributes" : { "name" : "Hello Thing", "temp" : 42} }"""
  val sessionHeaders = Map("Authorization" -> "Bearer ${authToken}", "Content-Type" -> "application/json")
  val skinnyTrafficPopulation = Seq(
    constantConcurrentUsers(10) during (10 seconds),
    rampConcurrentUsers(10) to (50) during (120 seconds))

  val basicScenarioSecured: ScenarioBuilder = scenario("Basic Test Secured")
    .exec(http("Authenticate")
      .post("/auth/login")
      .header("Content-Type", "application/json")
      .body(StringBody(authenticationRequest)).asJson
      .check(status.is(200))
      .check(jsonPath("$.token").exists.saveAs("authToken")))
    .pause(Duration.create("1 s"))
    .doIf("${authToken.exists()}") {
      exec(http("Create Thing")
        .post("/api/thing")
        .headers(sessionHeaders)
        .body(StringBody(thingCreationRequest)).asJson
        .check(status.is(201))
        .check(jsonPath("$.thingID").exists.saveAs("thingID")))
        .pause(Duration.create("1 s"))
    }

  setUp(basicScenarioSecured.inject(skinnyTrafficPopulation).protocols(httpProtocolClusteredViaZuulProxy))
}
