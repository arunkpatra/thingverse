# Thingverse Development - Quick Start

If you want to build Thingverse from the source code, follow the steps mentioned here.
The objective of the below steps are to build docker images from source, publish the images to
a locally running docker registry, and deploying all Thingverse components to a locally running
Kubernetes cluster. The steps below are primarily for developers wanting to configure a fully
working Thingverse environment and are familiar with the technology stack that Thingverse uses.

## System Requirements (for building and running Thingverse)

- Memory: 4 GB minimum, 8 GB recommended
- CPU Cores: 2 Cores, 4 Cores recommended
- Kubernetes: Version 1.16+
- Docker: Version 19+
- Java: 1.8 compatible, 11 recommended
- Gradle: Bundled via wrapper, but locally installed Gradle v1.65 helps.

## Build
- To run Thingverse components, you would need to ensure that a locally running Docker Registry is listening at port 5000. 
- Set an environment variable named **DOCKER_REGISTRY** to `localhost:5000`. 
- Start Docker, and your local Kubernetes cluster. You can install Docker Desktop if you like, Minikube and Microk8s are also ok.
- Now issue the following commands in a terminal window to build the docker images.

    ``` 
    git clone git@github.com:arunkpatra/thingverse.git
    
    cd thingverse
    ./gradlew build
    ./gradlew pushDockerImage
    ```

## Installation

### Prerequisites
- Start Docker and ensure the Docker Registry is listening at port 5000.
- Start your local Kubernetes cluster. You can install Docker Desktop(comes bundled with a single node Kubernetes cluster) if you like, Minikube and Microk8s are also ok.
- Verify using `docker ps` and `kubectl version`.
- Install Kubernetes Dashboard on to your cluster if you like (optional).
- Install Linkerd - Follow steps here: https://linkerd.io/2/getting-started/
- Issue following commands at the `thingverse` root directory to install all Thingverse components to your local Kubernetes cluster.

### Install
``` 
$ cd subprojects/thingverse-deployment/k8s/deployments/thingverse-aio
$ kubectl apply -f thingverse-aio.yaml
```
### Verify Installation
Verify if all went well, with the following commands(might take a while depending on the resources available to your Kubernetes cluster)
``` 
$ kubectl get pods -n thingverse-aio
```
After a while, if you see the following, congratulations, you have successfully installed a fully functional Thingverse instance on you local K8s cluster.

``` 
NAME                                        READY   STATUS    RESTARTS   AGE
cassandra-deployment-7476c4595-275pb        1/1     Running   0          2m25s
jaeger-5c77bbb648-j8ftm                     2/2     Running   0          2m25s
thingverse-admin-794d74c574-tj4tz           2/2     Running   0          2m25s
thingverse-api-59cd4cdcb6-phgcf             2/2     Running   0          2m25s
thingverse-backend-read-5b7c9557d4-ntvdd    2/2     Running   2          2m25s
thingverse-backend-write-58577d89f4-bwfnj   2/2     Running   2          2m25s
```

### Test Installation - Perform a few Test Operations

- Access Spring Boot Admin: http://localhost:30095
- Access Swagger UI for APIs: http://localhost:30091
- Make a few API calls: Either use Swagger UI or use `curl`. Example API call using `curl`:
    ``` 
    $ curl -X GET "http://localhost:30091/api/cluster/state" -H "accept: */*"
    
    # You should get the following response
    
    {"allMembersUp":true,"totalNodeCount":2,"readNodeCount":1,"writeNodeCount":1}   
    ```
- Access Linkerd Dashboard: First issue `linkerd dashboard &` on a terminal window. Now access http://localhost:50750/namespaces/thingverse-aio. You should be able to see your meshed deployments and live traffic.
- Access Jaeger UI for distributed Tracing: First issue `kubectl -n thingverse-aio port-forward svc/jaeger 16686 &` on a terminal window. Now access http://localhost:16686. You should be able to see distributed traces that spans process boundaries.
- Access Kubernetes Dashboard: First issue `kubectl proxy &` on a terminal window. Then access http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/overview?namespace=thingverse-aio. You should be able to see Kubernetes resources in the `thingverse-aio` namespace.

## Cleanup

To delete everything you just installed to your local Kubernetes cluster, issue the following commands in a terminal window from the root of the `thingverse` directory.
``` 
$ cd subprojects/thingverse-deployment/k8s/deployments/thingverse-aio
$ kubectl delete -f thingverse-aio.yaml 
```
