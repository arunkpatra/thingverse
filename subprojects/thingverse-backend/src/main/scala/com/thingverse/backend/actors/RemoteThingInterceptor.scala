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

package com.thingverse.backend.actors

import java.time.Instant

import akka.actor.typed.{PostStop, Signal, TypedActorContext}
import akka.cluster.sharding.typed.javadsl.ClusterSharding
import com.thingverse.api.command.ThingverseCommand
import com.thingverse.backend.api.interceptors.InterceptionHandler
import com.thingverse.backend.command.MonitoredThingverseCommand

class RemoteThingInterceptor extends InterceptionHandler[ThingverseCommand] {
  /**
   * Intercept the start of a RemoteThing.
   *
   * @param ctx The actor context of the actor that was started
   */
  override def handleStart(ctx: TypedActorContext[ThingverseCommand]): Unit = {
    val log = ctx.asScala.log
    log.debug(s"@@@ Actor ${ctx.asScala.self.path} starting...")
    ClusterSharding.get(ctx.asScala.system)
      .entityRefFor(MetricsCollector.ENTITY_TYPE_KEY, MetricsCollector.METRICS_COLLECTOR_ENTITY_ID)
      .tell(new MetricsCollector.IncrementCommand())
  }

  override def handleMessage(ctx: TypedActorContext[ThingverseCommand], msg: ThingverseCommand): Unit = {
    val log = ctx.asScala.log
    log.debug(s"@@@ Actor ${ctx.asScala.self.path} received new message: $msg")
    // We calculate metrics from only those messages which are meant to be monitored
    msg match {
      case m: MonitoredThingverseCommand =>
        m.setDeliveredToActorAt(Instant.now)
        log.debug(s"Message age of message $msg is ${m.getMessageAge} micro seconds.")
        ClusterSharding.get(ctx.asScala.system)
          .entityRefFor(MetricsCollector.ENTITY_TYPE_KEY, MetricsCollector.METRICS_COLLECTOR_ENTITY_ID)
          .tell(new MetricsCollector.MessageReceivedCommand(m))
      case _ => // NOOP
    }
  }

  override def handleSignal(ctx: TypedActorContext[ThingverseCommand], signal: Signal): Unit = {
    val log = ctx.asScala.log
    log.debug(s"@@@ Actor ${ctx.asScala.self.path} received new signal: $signal")
    signal match {
      case _: PostStop =>
        ClusterSharding.get(ctx.asScala.system)
          .entityRefFor(MetricsCollector.ENTITY_TYPE_KEY, MetricsCollector.METRICS_COLLECTOR_ENTITY_ID)
          .tell(new MetricsCollector.DecrementCommand())
      case _ => // NOOP
    }
  }
}
