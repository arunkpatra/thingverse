# Thingverse Akka Backend

### Quick Start
Startup Consul on your machine(running on port 8500). Startup Cassandra. Then fire up two backend instances, one with the 
`read-model` role and another with the `write-model` role. Note that, **Thingverse** adopts the CQRS design pattern and
hence the two backend instances.

``` 
# Read Backend instance
java -Xss256M -Dconfig.resource=application-discovery.conf -Dthingverse.backend.roles=read-model -Dthingverse.backend.akka-remote-port=0 -Dthingverse.backend.grpc-server-port=0 -Dthingverse.backend.akka-management-http-port=0 -Dthingverse.consul.registration.enabled=true -Dthingverse.storage.backend.cassandra.embedded=false -jar thingverse-backend.jar

# Write Backend instance
java -Xss256M -Dconfig.resource=application-discovery.conf -Dthingverse.backend.roles=write-model -Dthingverse.backend.akka-remote-port=0 -Dthingverse.backend.grpc-server-port=0 -Dthingverse.backend.akka-management-http-port=0 -Dthingverse.consul.registration.enabled=true -Dthingverse.storage.backend.cassandra.embedded=false -jar thingverse-backend.jar    
```

### Running the Akka Backend
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
1.  The simplest way to run a **standalone** instance of the Thingverse Akka backend, is to issue the following
command in a terminal window. You need to build the jar first using the `spring-boot-maven` plugin.
    ``` 
    cd thingverse-backend
    java -jar target/thingverse-backend.jar
    ```
    > The above command will run the Thingverse Akka backend with the following default configuration.
    >1.  Seed backend lookup: Static lookup at `["akka://thingverse-backend@127.0.0.1:2551","akka://thingverse-backend@127.0.0.1:2552"]`
    >2.  Akka Port: `2551`                                                                                                                                                                                                                                          
    >3.  gRPC Port: `8080`
    >4.  Akka Management Port: `8558`                                                                                                                                                                                                                                          
    >5.  Akka CQRS roles: `["read-model","write-model"]`
    >6.  Cassandra: `Embeded Cassandra running on port 9043`                                                                                                                                                                                                                                              
    >7.  Service Discovery via Consul: `None`

2.  **Run multiple backend nodes** in the Akka cluster. You can run as many instances as you want, but you need to specify different 
ports and. Also, you must have at least one backend node with `read-model` role and another with `write-model` role. You can 
also specify both roles in a single instance. Run the backend instances as follows. Note that, running the backend this way will not 
use consul-discovery to locate the akka cluster seed nodes. See the next point if you want to use service discovery 
using Consul. The end result is same in both cases though. Note that, in this case you **must** provide a running 
Cassandra instance, and you must switch off the embedded Cassandra instance using the property
`-Dthingverse.storage.backend.cassandra.embedded=false`.

    > Note that, in this case you **must** meet the following pre-conditions:
    >1.  Switch off embedded Cassandra using `-Dthingverse.storage.backend.cassandra.embedded=false`.
    >2.  Provision a Cassandra instance running on port `9043`.

    ``` 
    java -Dthingverse.storage.backend.cassandra.embedded=false -Dakka.cluster.roles.0=read-model -Dakka.cluster.roles.1=read-model -Dakka.cluster.backend.port=2551 -Dthingverse.grpc.server.port=8080 -Dakka.management.http.port=8558 -jar target/thingverse-backend.jar
    java -Dthingverse.storage.backend.cassandra.embedded=false -Dakka.cluster.roles.0=write-model -Dakka.cluster.roles.1=write-model -Dakka.cluster.backend.port=2552 -Dthingverse.grpc.server.port=8081 -Dakka.management.http.port=8559 -jar target/thingverse-backend.jar
    java -Dthingverse.storage.backend.cassandra.embedded=false -Dakka.cluster.roles.0=read-model -Dakka.cluster.roles.1=read-model -Dakka.cluster.backend.port=2553 -Dthingverse.grpc.server.port=8082 -Dakka.management.http.port=8560 -jar target/thingverse-backend.jar
    java -Dthingverse.storage.backend.cassandra.embedded=false -Dakka.cluster.roles.0=write-model -Dakka.cluster.roles.1=write-model -Dakka.cluster.backend.port=2554 -Dthingverse.grpc.server.port=8083 -Dakka.management.http.port=8561 -jar target/thingverse-backend.jar        
    ```
    > Note: In the above mentioned launch commands, the first two (backend.port 2551 and 2552) are the seed nodes. You must run these for
    the system to work. The other two nodes are optional and you can start those or many others depending on your hardware and
    test scenario.

3.  If you want to test **dynamic discovery of seed nodes** in the Akka cluster, you can use the following. Note that, 
these are similar to point 3 above, with the addition of the `-Dconfig.resource=application-discovery.conf` property. 

    > Note that, in this case you **must** meet the following pre-conditions:
    >1.  Switch off embedded Cassandra using `-Dthingverse.storage.backend.cassandra.embedded=false`.
    >2.  Provide a running Cassandra instance running on port `9043`.
    >3.  Ensure that Consul is running on your machine at port `8500`.

    ``` 
    java -Dthingverse.storage.backend.cassandra.embedded=false -Dthingverse.consul.registration.enabled=true -Dconfig.resource=application-discovery.conf -Dakka.cluster.roles.0=read-model -Dakka.cluster.roles.1=read-model -Dakka.cluster.backend.port=2551 -Dthingverse.grpc.server.port=8080 -Dakka.management.http.port=8558 -jar target/thingverse-backend.jar
    java -Dthingverse.storage.backend.cassandra.embedded=false -Dthingverse.consul.registration.enabled=true -Dconfig.resource=application-discovery.conf -Dakka.cluster.roles.0=write-model -Dakka.cluster.roles.1=write-model -Dakka.cluster.backend.port=2552 -Dthingverse.grpc.server.port=8081 -Dakka.management.http.port=8559 -jar target/thingverse-backend.jar
    java -Dthingverse.storage.backend.cassandra.embedded=false -Dthingverse.consul.registration.enabled=true -Dconfig.resource=application-discovery.conf -Dakka.cluster.roles.0=read-model -Dakka.cluster.roles.1=read-model -Dakka.cluster.backend.port=2553 -Dthingverse.grpc.server.port=8082 -Dakka.management.http.port=8560 -jar target/thingverse-backend.jar
    java -Dthingverse.storage.backend.cassandra.embedded=false -Dthingverse.consul.registration.enabled=true -Dconfig.resource=application-discovery.conf -Dakka.cluster.roles.0=write-model -Dakka.cluster.roles.1=write-model -Dakka.cluster.backend.port=2554 -Dthingverse.grpc.server.port=8083 -Dakka.management.http.port=8561 -jar target/thingverse-backend.jar        
    ```    
    
#### Running the provided Cassandra App (for testing)

Although, you can use the embedded Cassandra instance, it can only be used and useful while running the node in
**standalone** mode. If you are running the nodes as part of a cluster, all the nodes must use a single Cassandra instance
or Cassandra cluster. In this case, if you don't have Cassandra installed already on a machine, we ship an Cassandra app
which you can use for testing. Running this app will provision a single node Cassandra instance at port 9043.

    ``` 
    cd thingverse-test-cassandra-app
    mvn spring-boot:run
    ```
   > Note: You should run a dedicated Cassandra cluster when doing a larger test or in production environments.
  
#### Running using Docker

You can launch a docker container as well.

``` 
# Build the image

$ cd thingverse-backend
$ docker build --tag thingverse/thingverse-backend .
```

Run a container will all defaults. This uses an embedded Cassandra instance(port 9043), does not use Consul for service
discovery, Akka remote port at 2551, gRPC port at 8080 and Akka management port at 8558.

``` 
docker run -p 8080:8080 --name=thingverse-backend thingverse/thingverse-backend
```

Note that, when you want to communicate between API and Backend and when both are running in docker containers, you
will have to put the two containers in the same network. See this - 
https://docs.docker.com/network/network-tutorial-standalone/#use-user-defined-bridge-networks