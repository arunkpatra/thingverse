import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

// See https://medium.com/@vcomposieux/load-testing-gatling-tips-tricks-47e829e5d449

class MiniSimulation extends Simulation {

  private val baseUrl = System.getenv().getOrDefault("BASE_URL", "http://localhost:30091")
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
  /**
   * This is what this traffic profile intends to mimic.
   *
   * 1. We treat the system under test (Thingverse) as a closed model, meaning that number of max-concurrent-users
   * is capped. A "user" is an entity that interacts with Thingverse via its tcp accessible interfaces (e.g. REST/HTTP,
   * MQTT, gRPC/HTTP2) e.g. a physical network connected device or a software simulated device.
   *
   * This choice is meaningful since customers who provision a higher value of max-concurrent-users will have to
   * request provisioning of such requirements upfront so that the computation/storage/network resources can be wired
   * accordingly. This is the option that AWS provides to customers. Once the max-concurrent-users is hit, requests
   * will be throttled.
   *
   * 2. We vary the number of requests-per-second. A "request" means an operation that a user wants perform at
   * Thingverse (e.g. Create a Thing, Read State of a Thing etc.)
   *
   * 3. Our end goal is to:
   * a) Create N_max concurrently active Things (a.k.a Akka Actors) inside Thingverse for a given hardware/network
   * configuration. N_max is a pre-determined value. "Our primary intent is to see high values of N_max".
   *
   * b) See live graphs:
   * i) Current values of the number of Things in Thingverse (n)
   * ii) The rate at which n has increased over time.
   * iii) The requests per second arriving at Thingverse
   *
   * See https://gatling.io/docs/current/general/simulation_setup
   */
  val closedModelTrafficProfile = Seq(
    rampConcurrentUsers(1) to (8) during (8 seconds), // warm up the cluster, just a "kick-start"
    rampConcurrentUsers(8) to (16) during (5 seconds), // Switch to first gear
    constantConcurrentUsers(16) during (5 seconds), // Drive steady at first gear
    rampConcurrentUsers(16) to (32) during (5 seconds), // Switch to second gear
    constantConcurrentUsers(32) during (30 seconds)
  )

  val miniScenario: ScenarioBuilder = scenario("Mini Scenario")
    .exec(http("Create Thing.")
      .post("/api/thing")
      .body(StringBody(thingCreationRequest)).asJson
      .check(status.is(201))
      .check(jsonPath("$.thingID").exists))

  setUp(miniScenario.inject(closedModelTrafficProfile).protocols(httpProtocolStandalone))
}
