# Logging, tracing e masking

## Formato log JSON
Utilizzare log strutturati in JSON con i campi minimi per correlazione e multi-tenancy:

```json
{
  "timestamp": "2024-05-28T09:30:12.345Z",
  "severity": "INFO",
  "service.name": "condominio-api",
  "trace_id": "4fd0b7f1c0f8d6c4d9c0d5d3b1e2f3a4",
  "span_id": "9b2d1a0f3c4e5d6a",
  "tenant.id": "tenant-42",
  "http.method": "POST",
  "http.route": "/api/v1/tickets",
  "message": "ticket created",
  "user.id": "a1b2c3"
}
```

### Propagazione trace-id
- Abilitare l'instrumentation OpenTelemetry del linguaggio usato con propagazione W3C `traceparent`/`tracestate`.
- In ingresso leggere `traceparent`; in uscita includerlo in response e in chiamate downstream.
- Il collector (`observability/otel-collector.yaml`) accetta OTLP HTTP/gRPC e aggiunge attributi di ambiente/tenant.

### Gestione tenant
- Normalizzare l'header `X-Tenant-ID` in un attributo di resource `tenant.id` in ogni richiesta.
- Per applicazioni web usare middleware che validi l'header e lo inserisca nel contesto prima della creazione dello span.
- Per job/batch leggere la variabile `TENANT_ID` e valorizzarla nel tracer provider.

## Masking dati sensibili
- Il collector usa il processor `redaction` per mascherare pattern di carta/IBAN/email.
- Nel codice applicativo evitare di loggare payload completi; loggare solo identificatori e metadati.
- Per i campi sensibili necessari (es. ultimi 4 cifre), applicare mascheramento prima di inviare il log.

## Metriche e traces
- Instrumentare HTTP server/client, DB e code (es. Redis, Kafka) usando le auto-instrumentation OpenTelemetry.
- Esportare metriche e traces verso il collector OTLP; il collector li invia a remote-write Prometheus e OTLP backend.
- Aggiungere attributi `service.namespace` e `deployment.environment` per filtrare i dati in dashboard/alert.

## Controlli rapidi
- Verifica locale: avvia il collector `otelcol-contrib --config observability/otel-collector.yaml` impostando le variabili `OTLP_ENDPOINT` e `PROMETHEUS_REMOTEWRITE_ENDPOINT`.
- Conferma log mascherati controllando l'exporter `logging` del collector (livello warn). 
