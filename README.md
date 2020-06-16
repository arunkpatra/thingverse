[![Apache License 2](https://img.shields.io/badge/license-ASF2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Build Status](https://travis-ci.org/arunkpatra/thingverse.svg?branch=master)](https://travis-ci.org/arunkpatra/thingverse)
[![Coverage Status](https://coveralls.io/repos/github/arunkpatra/thingverse/badge.svg?branch=master)](https://coveralls.io/github/arunkpatra/thingverse?branch=master)
[![Join the chat at https://gitter.im/thingverse/community](https://badges.gitter.im/thingverse/community.svg)](https://gitter.im/thingverse/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

<img src="thingverse.png" width="350px" alt="Thingverse Logo" />

The Thingverse platform allows virtualizing physical things. It allows you 
to interact with real things via their virtual counterparts and build higher
level functions as per your business needs. Thingverse is business domain agnostic. 
It works at extreme levels of concurrency, is self-healing, resilient and scales to
billions of things while using the minimum possible compute and memory resources. Whether on-prem or the largest
Kubernetes Cluster in the cloud, its the same for Thingverse.

## Documentation

## Goals

*  Virtualize physical things and provide connectivity mechanisms with real
   things.
*  Spawn a large number of virtual things and interact with them.
*  Ability to observe events associated with things and perform user specified
   actions.

### System Requirements

- Memory: 4 GB minimum, 8 GB recommended
- CPU Cores: 2 Cores, 4 Cores recommended
- Kubernetes: Version 1.16+
- Docker: Version 19+
- Java: 1.8 compatible, 11 recommended
- Gradle: Bundled via wrapper, but version 1.65 locally installed helps.

## Getting Started

### Build
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

### Installation

#### Prerequisites
- Start Docker and ensure the Docker Registry is listening at port 5000.
- Start your local Kubernetes cluster. You can install Docker Desktop(comes bundled with a single node Kubernetes cluster) if you like, Minikube and Microk8s are also ok.
- Verify using `docker ps` and `kubectl version`.
- Install Kubernetes Dashboard on to your cluster if you like (optional).
- Install Linkerd - Follow steps here: https://linkerd.io/2/getting-started/
- Issue following commands at the `thingverse` root directory to install all Thingverse components to your local Kubernetes cluster.

#### Install
    ``` 
    $ cd subprojects/thingverse-deployment/k8s/deployments/thingverse-aio
    $ kubectl apply -f thingverse-aio.yaml
    ```
#### Verify
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

#### Test Installation

- Access Spring Boot Admin: http://localhost:30095
- Access Swagger UI for APIs: http://localhost:30091
- Make a few API calls: Either use Swagger UI or use `curl`. Example API call using `curl:
    ``` 
    $ curl -X GET "http://localhost:30091/api/cluster/state" -H "accept: */*"
    
    # You should get the following response
    
    {"allMembersUp":true,"totalNodeCount":2,"readNodeCount":1,"writeNodeCount":1}   
    ```
- Access Linkerd Dashboard: First issue `linkerd dashboard &` on a terminal window. Now access http://localhost:50750/namespaces/thingverse-aio. You should be able to see your meshed deployments and live traffic.
- Access Jaeger UI for distributed Tracing: First issue `kubectl -n thingverse-aio port-forward svc/jaeger 16686 &` on a terminal window. Now access http://localhost:16686. You should be able to see distributed traces that spans process boundaries.
- Access Kubernetes Dashboard: First issue `kubectl proxy &` on a terminal window. Then access http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/overview?namespace=thingverse-aio. You should be able to see Kubernetes resources in the `thingverse-aio` namespace.

#### Cleanup

To delete everything you just installed to your local Kubernetes cluster, issue the following commands in a terminal window from the root of the `thingverse` directory.
``` 
$ cd subprojects/thingverse-deployment/k8s/deployments/thingverse-aio
$ kubectl delete -f thingverse-aio.yaml 
```

## Getting Help
Head over to Gitter [![Join the chat at https://gitter.im/thingverse/community](https://badges.gitter.im/thingverse/community.svg)](https://gitter.im/thingverse/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge). If you run into problems, feel free to raise an issue.

## Trademarks and licenses
The source code of Thingverse is licensed under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

## Screenshots

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md) file.
