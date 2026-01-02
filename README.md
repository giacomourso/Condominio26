# Condominio26

Set di configurazioni per abilitare osservabilità, sicurezza e compliance.

## Osservabilità
- **OpenTelemetry Collector**: `observability/otel-collector.yaml` riceve trace/log/metriche via OTLP (HTTP/gRPC), applica masking (`redaction`), arricchisce con `tenant.id`, `service.namespace`, `deployment.environment` e invia a OTLP backend + Prometheus Remote Write.
- **Logging**: linee guida e payload JSON con trace-id/span-id/tenant in `observability/logging.md`.
- **Grafana**: dashboard pronta all'import `observability/grafana-dashboard.json` con RPS, error-rate, latenza p95, risorse pod e traffico per tenant.
- **Prometheus**: regole di alert in `observability/prometheus-alerts.yaml` (error-rate, latenza, spike traffico tenant, restart loop, PodSecurity, collector down).

## Sicurezza CI/CD
Workflow `./github/workflows/security.yml`:
- **OWASP Dependency-Check** (SCA/SAST) su repo.
- **Trivy filesystem** per vulnerabilità critiche/alte.
- **Trivy image** opzionale (set env `IMAGE_NAME` come variabile del repo).
- **ZAP baseline DAST** opzionale (set env `DAST_TARGET_URL`).
- **Policy lint** per i manifesti Kyverno/PodSecurity.

## Policy Kubernetes
- **Immagini base approvate** e divieto tag `latest`: `policies/kyverno-allowed-base-images.yaml` (Kyverno ClusterPolicy).
- **PodSecurity Admission**: namespace esempio con enforcement `restricted`/`baseline` in `policies/podsecurity-namespaces.yaml`.

## Avvio locale collector
```bash
export OTLP_ENDPOINT=\"https://otel.example.com:4317\"
export PROMETHEUS_REMOTEWRITE_ENDPOINT=\"https://prometheus.example.com/api/v1/write\"
export TENANT_ID=tenant-42
export DEPLOYMENT_ENVIRONMENT=dev
otelcol-contrib --config observability/otel-collector.yaml
```
