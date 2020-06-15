## HELM docs

https://helm.sh/docs/intro/using_helm/

A _**Chart**_ is a Helm package. It contains all of the resource definitions necessary to run an application, tool, or service inside of a Kubernetes cluster. Think of it like the Kubernetes equivalent of a Homebrew formula, an Apt dpkg, or a Yum RPM file.

A _**Repository**_ is the place where charts can be collected and shared. It's like Perl's CPAN archive or the Fedora Package Database, but for Kubernetes packages.

A _**Release**_ is an instance of a chart running in a Kubernetes cluster. One chart can often be installed many times into the same cluster. And each time it is installed, a new release is created. Consider a MySQL chart. If you want two databases running in your cluster, you can install that chart twice. Each one will have its own release, which will in turn have its own release name.

Helm installs _**charts**_ into Kubernetes, creating a new _**release**_ for each installation. And to find new charts, you can search Helm chart _**repositories**_.

### References

See Cassandra: https://hub.helm.sh/charts/bitnami/cassandra


### Guides

[Quick Start Guide][quick_start]

Get Help

``` 
helm get -h
```

Once you have Helm ready, you can add a chart repository. One popular starting location is the official Helm stable charts:

``` 
$ helm repo add stable https://kubernetes-charts.storage.googleapis.com/
$ helm search repo stable
$ helm repo update
$ helm install stable/mysql --generate-name
```

It's easy to see what has been released using Helm:

``` 
$ helm ls
NAME             VERSION   UPDATED                   STATUS    CHART
smiling-penguin  1         Wed Sep 28 12:59:46 2016  DEPLOYED  mysql-0.1.0
```

Uninstall a release
``` 
$ helm uninstall smiling-penguin
Removed smiling-penguin
```


[quick_start]: https://helm.sh/docs/intro/quickstart/