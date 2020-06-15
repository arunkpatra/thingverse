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