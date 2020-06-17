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

package com.thingverse.backend.api.interceptors

import akka.actor.typed.{Signal, TypedActorContext}

trait InterceptionHandler[T] {

  /**
   * Actor start interceptor.
   *
   * @param ctx The actor context of the actor that was started
   */
  def handleStart(ctx: TypedActorContext[T])

  /**
   * Message interceptor. All messages are intercepted. Its upto the implementation to filter and process as required.
   *
   * @param ctx The actor context
   * @param msg The message for the actor
   */
  def handleMessage(ctx: TypedActorContext[T], msg: T)

  /**
   * Signal interceptor. All signals are intercepted. Its upto the implementation to filter and process as required.
   *
   * @param ctx    The actor context.
   * @param signal The received signal.
   */
  def handleSignal(ctx: TypedActorContext[T], signal: Signal)
}
