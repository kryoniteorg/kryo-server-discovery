# kryo-server-discovery

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=kryoniteorg_kryo-server-discovery&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=kryoniteorg_kryo-server-discovery)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=kryoniteorg_kryo-server-discovery&metric=coverage)](https://sonarcloud.io/summary/new_code?id=kryoniteorg_kryo-server-discovery)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=kryoniteorg_kryo-server-discovery&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=kryoniteorg_kryo-server-discovery)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=kryoniteorg_kryo-server-discovery&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=kryoniteorg_kryo-server-discovery)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=kryoniteorg_kryo-server-discovery&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=kryoniteorg_kryo-server-discovery)

This plugin connects minecraft servers to a velocity proxy within Kubernetes.

The service account in the namespace which the proxy is running in needs the privilege to read
endpoints from the namespaces where the minecraft servers are running in

## Setup

Servers are discovered by labels. The namespaces where the Minecraft servers are running in, have to be
labeled with `server-discovery: "true"`. The pods which the minecraft server is running in has to be labeled
with `server-discovery: "true"`
as well.

## Configuration

kryo-server-discovery can currently be configured using the following environment variables:

| Variable                            | Default Value | Description                                                                                    |
|-------------------------------------|---------------|------------------------------------------------------------------------------------------------|
| `KRYO_SV_ENABLE_JOIN_LISTENER`      | true          | This enables the Join Listener and will send the player to a random discovered server on join. |
| `KRYO_SV_DISCOVER_TASK_INTERVAL_MS` | 1000          | This is the interval between the times the plugin polls the k8s api to discover new servers.   |
| `KRYO_SV_SERVER_NAME_FORMAT`        | %s            | Java format string allowing the prefixing or suffixing of the name of the discovered servers.  |

## Examples

Namespace:

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: minecraft
  labels:
    server-discovery: "true"
```

Deployment:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: minecraft
  namespace: minecraft
  labels:
    app: minecraft
spec:
  replicas: 1
  selector:
    matchLabels:
      app: minecraft
  template:
    metadata:
      labels:
        app: minecraft
        server-discovery: "true"
    spec:
      containers:
      - name: minecraft
        image: minecraft
```

## Role and role bindings

Role in the namespace where the minecraft servers are running in:

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: minecraft
  name: minecraft-role
rules:
  - apiGroups: [""]
    resources: ["pods"]
    verbs: ["list"]
```

Corresponding role binding to the namespace where the proxy is running in:

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: minecraft-role-binding
  namespace: minecraft
subjects:
  - kind: ServiceAccount
    name: default
    namespace: proxy
roleRef:
  kind: Role
  name: minecraft-role
  apiGroup: rbac.authorization.k8s.io
```

A cluster role has to be defined to list namespaces:

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: namespace-reader
rules:
- apiGroups: [""]
  resources: ["namespaces"]
  verbs: ["list"]
```

The role binding for the cluster role could look like the following:

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: read-namespace-global
subjects:
  - kind: ServiceAccount
    name: default
    namespace: proxy
roleRef:
  kind: ClusterRole
  name: namespace-reader
  apiGroup: rbac.authorization.k8s.io
```
