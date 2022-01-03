# kryo-server-discovery

This plugin connects minecraft servers to a velocity proxy within Kubernetes.

The service account in the namespace which the proxy is running in needs the privilege to read
endpoints from the namespaces where the minecraft servers are running in

## Setup
Minecraft servers are discovered by labels. The namespaces where the Minecraft servers are running in have to be
labeled with `server-discovery: "true"`. The pods which the minecraft server is running in has to be labeled with `server-discovery: "true"`
as well.

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
