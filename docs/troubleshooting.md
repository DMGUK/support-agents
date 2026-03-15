# Troubleshooting Guide

## Common Error Codes and Fixes

### ERR-001: Connection Refused

**Symptom:** The agent service fails to start and logs show `Connection refused on port 8443`.

**Cause:** Another process is using port 8443, or a firewall is blocking the port.

**Fix:**
1. Check what is using the port: `lsof -i :8443` (Linux/macOS) or `netstat -ano | findstr 8443` (Windows).
2. If it is another process, stop it or change the agent port in `config.yaml` under `server.port`.
3. Ensure your firewall allows outbound HTTPS (443) and the internal service port (8443).

### ERR-002: Invalid Workspace ID

**Symptom:** Health check returns `[FAIL] Workspace not found`.

**Cause:** The `workspace_id` in `config.yaml` does not match any workspace in your account.

**Fix:** Log in to the dashboard, go to **Settings → Workspace**, and copy the exact ID string. Workspace IDs are case-sensitive and follow the format `ws_XXXXXXXXXX`.

### ERR-003: TLS Certificate Error

**Symptom:** Logs contain `PKIX path building failed` or `SSL handshake error`.

**Cause:** Your system's Java truststore does not include the platform's certificate authority.

**Fix:**
1. Download the CA bundle from `https://api.support-platform.example.com/ca-bundle.pem`.
2. Import it: `keytool -importcert -alias sp-ca -file ca-bundle.pem -keystore $JAVA_HOME/lib/security/cacerts`
3. Default keystore password is `changeit`.
4. Restart the agent service.

### ERR-004: Webhook Delivery Failures

**Symptom:** Webhooks configured in the dashboard are not being received.

**Cause:** The target URL is unreachable, or it returned a non-2xx status code.

**Fix:**
1. Under **Settings → Webhooks**, check the delivery log for the specific failure status code.
2. Ensure your endpoint responds within 10 seconds (our timeout).
3. Return HTTP 200 immediately and process the payload asynchronously if needed.
4. Whitelist our IP ranges: `52.18.0.0/16` and `34.240.0.0/16`.

### ERR-005: High Latency / Slow Responses

**Symptom:** API calls take more than 5 seconds consistently.

**Cause:** Region mismatch between the client and the API endpoint.

**Fix:** Set `region` in `config.yaml` to the region closest to your servers. Available regions: `us-east-1`, `eu-west-1`, `ap-southeast-1`.

## Log Locations

- Windows: `%ProgramData%\SupportPlatform\logs\agent.log`
- Linux: `/var/log/support-platform/agent.log`
- macOS: `~/Library/Logs/SupportPlatform/agent.log`

Set `log_level: debug` in `config.yaml` for verbose output during troubleshooting.