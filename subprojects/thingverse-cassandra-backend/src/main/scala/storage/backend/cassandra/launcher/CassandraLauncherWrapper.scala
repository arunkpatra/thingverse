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

package storage.backend.cassandra.launcher

import java.io.File

import akka.persistence.cassandra.testkit.CassandraLauncher
import org.slf4j.LoggerFactory
import storage.backend.cassandra.config.CassandraBackendProperties

object CassandraLauncherWrapper {

  private val LOGGER = LoggerFactory.getLogger(classOf[CassandraLauncherWrapper])

  def start(properties: CassandraBackendProperties): Unit = {
    LOGGER.info(">>> Using config file: {}", properties.getConfigFile)
    val databaseDirectory = new File(properties.getPath)
    CassandraLauncher.start(
      databaseDirectory,
      properties.getConfigFile,
      properties.isStartClean,
      properties.getPort,
      Nil,
      Some(properties.getAddress))
    LOGGER.info(s"Started Cassandra with port: ${properties.getPort}, path: ${properties.getPath}, " +
      s"bind address: ${properties.getAddress}.")
  }

  def stop(): Unit = {
    LOGGER.info("Stopping Cassandra daemon.")
    CassandraLauncher.stop()
    LOGGER.info("Stopped Cassandra daemon.")
  }
}

class CassandraLauncherWrapper