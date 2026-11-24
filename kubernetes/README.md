# SmartRecruit on Kubernetes

Everything the platform needs runs in the `smartrecruit` namespace:

| File | What it runs |
|---|---|
| `namespace.yaml`, `config.yaml` | Namespace and the shared, non-secret settings (`smartrecruit-config`) |
| `secrets.example.yaml` | Template for the `smartrecruit-secrets` Secret (never applied automatically) |
| `infra/postgres.yaml` | PostgreSQL with one database per service, created on first start |
| `infra/kafka.yaml` | Kafka (KRaft, single node) for the `smartrecruit.domain-events` topic |
| `infra/minio.yaml` | MinIO for private CV and employment-proof files |
| `*-service.yaml` | One Deployment and one ClusterIP Service per backend service, all on port 8080 |
| `api-gateway/` | The public API entry point, two replicas |
| `frontend.yaml` | The Angular app served by nginx, two replicas |
| `ingress.yaml` | `https://<host>/` to the app and `https://<host>/api` to the gateway |

Services find each other through Kubernetes DNS (`http://recruitment-service:8080`, …). No Eureka or discovery client is needed.

## 1. Build and push the images

From the backend repository root, once per service:

```text
docker build -t <registry>/smartrecruit/identity-service:<tag> --build-arg MODULE=services/identity-service --build-arg APP_JAR=identity-service .
```

Repeat for `recruitment-service`, `candidate-ai-service`, `subscription-service`, `interview-service`, `notification-service`, `analytics-service` and `api-gateway`. Build the frontend from `SmartRecruit-Stitch-Angular` with its own `Dockerfile` as `smartrecruit/frontend`. Push them, then set the registry and tag in the `images:` list of `kustomization.yaml`.

## 2. Create the Secret

```text
kubectl create namespace smartrecruit
kubectl -n smartrecruit create secret generic smartrecruit-secrets \
  --from-literal=identity-jwt-secret="$(openssl rand -base64 48)" \
  --from-literal=postgres-password='<strong password>' \
  --from-literal=admin-password='<strong password>' \
  --from-literal=minio-root-user='smartrecruit' \
  --from-literal=minio-root-password='<strong password>'
```

## 3. Set your host and deploy

Replace `smartrecruit.example.com` in `ingress.yaml` and `CORS_ALLOWED_ORIGINS` in `config.yaml`, then:

```text
kubectl apply -k kubernetes/
kubectl -n smartrecruit get pods -w
```

Services become ready when `/actuator/health` answers (database reachable). Start-up probes give each Java service up to three minutes.

## Notes for production

- **Managed data stores:** to use a managed PostgreSQL, Kafka or S3, remove the matching `infra/` file from `kustomization.yaml` and change the URLs in the services and `config.yaml`.
- **Scaling:** the gateway and frontend are stateless and can scale freely. Recruitment drains its event outbox with a scheduler and Identity creates the first administrator at start-up, so keep those at one replica unless you add leader election.
- **Resilience:** Interview calls Recruitment through a Feign client with 2 s connect and 5 s read timeouts, one retry for network errors on GET requests, and a circuit breaker that fails fast with a 503 when Recruitment is down.
