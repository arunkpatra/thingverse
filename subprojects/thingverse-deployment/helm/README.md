## Notes

#### Preparing for a Release
You need to build a local image and push it to a docker registry first.

##### Push images to local Docker Registry
``` 
# List local images known to local Docker daemon
$ docker image ls
REPOSITORY                           TAG                     IMAGE ID            CREATED             SIZE
thingverse-api                       1.0.0                   1887dd62636d        4 hours ago         380MB
localhost:5000/thingverse-api        1.0.0                   1887dd62636d        4 hours ago         380MB
thingverse-backend                   1.0.0                   18546b963805        4 hours ago         393MB
localhost:5000/thingverse-backend    1.0.0                   18546b963805        4 hours ago         393MB

# Tag images
docker tag 1887dd62636d localhost:5000/thingverse-api:1.0.0
docker tag 18546b963805 localhost:5000/thingverse-backend:1.0.0

# Push images to local docker registry
docker push localhost:5000/thingverse-api:1.0.0
docker push localhost:5000/thingverse-backend:1.0.0

# Verify that images got pushed
$ curl http://localhost:5000/v2/thingverse-api/manifests/1.0.0 -o thingverse-api.txt
$ curl http://localhost:5000/v2/thingverse-backend/manifests/1.0.0 -o thingverse-backend.txt

```

If the `curl` command writes the following text in the output file, it means image has not been pushed to the Docker
registry.

``` 
{"errors":[{"code":"MANIFEST_UNKNOWN","message":"manifest unknown","detail":{"Tag":"1.0.0"}}]} 
```

The output should contain among other things, the following data:

``` 
{
    "schemaVersion": 1,
    "name": "thingverse-api",
    "tag": "1.0.0",
    "architecture": "amd64",
    "fsLayers": [
        ...
    ],
    "history": [
        ...
    ],
    "signatures": [
        ...
    ]
}
```

#### Install (Perform a Release)
``` 
helm install thingverse-local-k8s thingverse --namespace thingverse --create-namespace 
helm install -f localregistry.yaml thingverse-local-k8s ./thingverse --namespace thingverse --create-namespace 
```

To run with external Cassandra cluster (not deployed by this chart), without monitoring, try the following. But first, ensure that the Cassandra cluster is running.:

``` 
$ helm install -f localregistry.yaml -f external-cassandra-local.yaml thingverse-local-k8s ./thingverse --namespace thingverse --create-namespace --dry-run
```
NOTE: the `-f file-name.yaml` is an override file which contain properties. These properties override the values in
`values.yaml`. Also the `--create-namespace` works in HELM 3.2.0 and above.

#### Run Tests for a Release
``` 
$ helm test thingverse-local-k8s --namespace thingverse --logs

Pod thingverse-local-k8s-test-connection pending
Pod thingverse-local-k8s-test-connection pending
Pod thingverse-local-k8s-test-connection succeeded
NAME: thingverse-local-k8s
LAST DEPLOYED: Mon May  4 22:08:28 2020
NAMESPACE: thingverse
STATUS: deployed
REVISION: 1
TEST SUITE:     thingverse-local-k8s-test-connection
Last Started:   Mon May  4 22:10:06 2020
Last Completed: Mon May  4 22:10:13 2020
Phase:          Succeeded
NOTES:
```
#### Get Status of a Release
``` 
$ helm status thingverse-local-k8s --namespace thingverse

NAME: thingverse-local-k8s
LAST DEPLOYED: Mon May  4 21:21:44 2020
NAMESPACE: thingverse
STATUS: deployed
REVISION: 3
TEST SUITE: None
NOTES:
Thank you for installing thingverse.
```
#### List Resources created by a Release
``` 
$ helm get manifest thingverse-local-k8s --namespace thingverse | kubectl get --namespace thingverse -f -

NAME                                    SECRETS   AGE
serviceaccount/thingverse-svc-account   1         13m

NAME                     TYPE       CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
service/thingverse-api   NodePort   10.100.104.55   <none>        9191:30091/TCP   13m

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/thingverse-api   1/1     1            1           13m
```

#### Upgrade a Release
``` 
helm upgrade -f winlocalregistry.yaml --install thingverse-local-k8s ./thingverse --namespace thingverse --force
```

#### List all Releases
``` 
$ helm ls --namespace thingverse
NAME                    NAMESPACE       REVISION        UPDATED                                 STATUS          CHART                   APP VERSION
thingverse-local-k8s    thingverse      2               2020-05-04 21:14:38.7371735 +0530 IST   deployed        thingverse-0.1.0        1.0.0
```

#### Show Release History
``` 
$ helm history thingverse-local-k8s --namespace thingverse

REVISION        UPDATED                         STATUS          CHART                   APP VERSION     DESCRIPTION
1               Mon May  4 21:10:33 2020        superseded      thingverse-0.1.0        1.0.0           Install complete
2               Mon May  4 21:14:38 2020        deployed        thingverse-0.1.0        1.0.0           Upgrade complete
```

#### Rollback a Release
``` 
$ helm rollback thingverse-local-k8s 1 --namespace thingverse 

Rollback was a success! Happy Helming!

$ helm history thingverse-local-k8s --namespace thingverse
```

#### Uninstall a Release
``` 
helm uninstall thingverse-local-k8s --namespace thingverse
```

#### Storage
https://kubernetes.io/docs/tasks/configure-pod-container/configure-persistent-volume-storage/

List persistent volumes
``` 
$ kubectl get pv -n thingverse

NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                           STORAGECLASS   REASON   AGE
thingverse-pv                              200Mi      RWO            Retain           Bound    thingverse/thingverse-api-pvc   hostpath                2m48s
```

List persistent volume claims

#### Delete a Pod

``` 
$ kubectl delete pod <pod_name> --namespace thingverse
```