### Thingverse Deployment

The Thingverse Deployment Guide is available [here](./src/docs/asciidoc/index.adoc). To generate the guide in `html` and `pdf` format, use the following command.

``` 
$ cd subprojects/thingverse-deployment
$ gradle generateThingverseDeploymentDocs
```

### Deploy Using HELM Charts

``` 
$ cd helm
```

#### Docker

Local repository

1. https://docs.docker.com/registry/deploying/

``` 
$ docker run -d -p 5000:5000 --restart=always --name registry registry:2
```