# Thingverse Zuul Proxy

When you run multiple API nodes, you would want to load balance traffic over them. You do that either by using a 
hardware or software load balancer traditionally. But we want to run an arbitrary number of instances of the API
somewhere in the network and discover them dynamically. In such a scenario, you need a Service Discover component
in addition to a load balancer that supports Service Discovery. We use **Consul** for service discovery and **Zuul Proxy**
to load balance.

### Running the Zuul Proxy

1.  Before running Zuul Proxy, you must start **Consul**, by issuing the following command.
    ``` 
    consul agent -dev -node machine -log-level info
    ```
2.  Run Zuul Proxy as follows.
    ``` 
    cd thingverse-zuul-proxy
    mvn spring-boot:run    
    ```
