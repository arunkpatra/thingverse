### Notes

Two possible approaches.

#### Approach 1

This approach currently works.

In this approach we would run Consul inside the k8s cluster.

1. We would run Consul inside the cluster, so run the Consul K8s deployment for this in the
local K8s cluster on mbp13.local.

2.  Cassandra will run on tp2.local. Will be available on LAN
at tp2.local:30090

3. On mbp13.local, we will run API and Backend in its local Kubernetes Cluster. 
Use Consul address as `consul.thingverse.svc.cluster.local` in your YAML files. 
Start the backend and API nodes
 in the cluster.

#### Approach 2

 _**This approach does not work. The services register with Consul with their cluster local
 addresses which the Consul instance running on the host can not resolve.**_
 
In this approach we would run Consul on the host machine , e.g. mbp13.local.

1. Launch consul in the following way.
    ```
    consul agent -dev -node mbp13-local -log-level warn -bind=192.168.0.108 -advertise=192.168.0.108 -client=192.168.0.108
    ```
2. Cassandra will run on tp2.local. Will be available on LAN at tp2.local:30090.

3. On mbp13.local, we will run API and Backend in its local Kubernetes Cluster. 
Use Consul address as `mbp13.local` in your YAML files. 
Start the backend and API nodes
 in the cluster.