# DOES NOT WORK

# Cassandra
A Cassandra Web UI Chart for Kubernetes

## Install Chart
To install the Cassandra Web UI Chart into your Kubernetes cluster

```bash
helm install cassandra-webui ./cassandra-webui --namespace thingverse --create-namespace --dry-run
```

After installation succeeds, you can get a status of Chart

```bash
helm status cassandra-webui --namespace thingverse
```

If you want to delete your Chart, use this command
```bash
helm delete  cassandra-webui --namespace thingverse
```