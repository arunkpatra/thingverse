# Thingverse API

The Thingverse API exposes REST endpoints to interact with your Akka cluster. The API app can either run standalone
or in a cluster mode.

### Running API

1.  The **standalone** mode is suitable for a quick verification of the APIs and
would serve all endpoints correctly. You would first need to build the jar using the `spring-boot` maven plugin. After
that, issue the following commands.
    ```
    cd thingverse-api
    java -jar target/thingverse-api.jar
    ```
    >In this mode, the server starts at port `9090` and the Swagger UI endpoint would be available at
    available at http://localhost:9090/swagger-ui.html. Note that, the API routes requests to the
    Akka backend. If the backend Akka backend are not running, you will still get valid responses but
    the response will indicate that the backend is unavailable.                                                                                                                                                                                                                                                                                                                                    

2.  When you need to run multiple nodes of the API in a cluster, either for HA or load balancing, you would need to
meet a few pre-requisites.
    >1.  Ensure that Consul is running on the local machine on port 8500. You can start a Consul
         server simply bys issuing `consul agent -dev -node machine -log-level info` on a terminal window.
    >2.  You have the means to front the API nodes with a load balancer working as a proxy.

    Once you are ready with the above, issue the following commands.                                                                                                                                                   
    ``` 
    cd thingverse-api
    java -Dspring.cloud.consul.enabled=true -Dthingverse.grpc.client.discovery-mechanism=consul -Dserver.port=0 -jar target/thingverse-api.jar
    #
    # or even the following will do
    #
    java -Doperation-mode=cluster -jar target/thingverse-api.jar
    ```                                                                                                                                              
    This will run the API server on a random port and register itself with Consul. You can see the service at                                                                                                                                               
    http://localhost:8500/ui. Typically you will multiple such instances and front them by a proxy. You can pick up
    the port number from Consui UI. The Swagger UI for this instance will be available at
    http://localhost:9999/swager-ui.html. Replace `9999` by the actual port number of this instance. In the example
    above, we are asking the API to look for backend Akka backends via Consul discovery lookup. If you don't supply
    the `-Dthingverse.grpc.client.discovery-mechanism=consul` setting, then a static discovery mechanism would be used
    to locate the backend akka backends.
              
3.  Run Zuul Proxy. In case you are running multiple instances of the API, you would need to load balance them using 
Zuul Proxy. Head over to the `thingverse-zuul-proxy` module to see the instructions on running the Zuul proxy.

### Running using Docker Image

```
cd thingverse-api
gradle build
docker build --tag thingverse/thingverse-api .
# This runs the image in interactive mode
docker run -it -p 9090:9090 --rm --env thingverse.api.secured=true arunkpatra/thingverse-api env
# This launches the image and returns. You can stop the image later
docker run -d -p 9090:9090 --name=thingverse-api --env thingverse.api.secured=true arunkpatra/thingverse-api
```

#### Helpful Docker commands

Note that, depending on how Docker has been installed on your machine, you would need admin or `sudo` access to most of 
these command below.

```
# Lists all images
docker image ls

# Removes an image
docker rmi <instance_id>

# Returns all containers
docker ps -a

# Remove a container
docker rm <container_id>

# SSH into a running container
docker exec -it <container name> /bin/bash

# Tail/Follow logs of running container
docker container logs --follow <container_name>

# Stop running container
docker stop <container_name>
```