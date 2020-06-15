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
