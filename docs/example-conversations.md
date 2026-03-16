# Example Conversations

Real output from the system on the `feature/djl-local-embeddings` branch.
Semantic search powered by DJL ONNX Runtime — no external API key required.

---

## Conversation 1 — Full system demo
```
Loaded 4 chunks from docs
Local embedding model ready.
Computing embeddings for 4 chunks...
Embeddings ready.
[Retriever] Semantic search enabled (DJL local embeddings)

You: My HubSpot integration keeps failing with a 401 error
  [Routing to Technical Specialist]

Agent: A 401 Unauthorized error means your HubSpot token has expired or been revoked.

Fix:
1. Go to HubSpot → Settings → Private Apps
2. Find your Support Platform app and regenerate the Access Token
3. Go to Support Platform → Integrations → HubSpot → Edit
4. Enter the new token and click Test Connection
5. You should see a green Connected badge

---

You: How do I fix ERR-003?

Agent: ERR-003 is a TLS Certificate Error — your Java truststore doesn't
include the platform's certificate authority.

Fix:
1. Download: https://api.support-platform.example.com/ca-bundle.pem
2. Import: keytool -importcert -alias sp-ca -file ca-bundle.pem
         -keystore $JAVA_HOME/lib/security/cacerts (password: changeit)
3. Restart the agent service

Resolves: PKIX path building failed, SSL handshake errors.

---

You: What are the API rate limits for the Pro plan?

Agent: Pro plan: 120 requests/minute, 50,000 requests/day.
If exceeded: HTTP 429 with Retry-After header.

| Plan       | Requests/minute | Requests/day |
|------------|-----------------|--------------|
| Free       | 10              | 500          |
| Pro        | 120             | 50,000       |
| Enterprise | Unlimited       | Unlimited    |

---

You: I want a refund
  [Routing to Billing Specialist]

Agent: I can help with that. Could you please provide your customer ID?

---

You: my customer ID is CUST-1234
  [tool_call] get_plan_info({customer_id=CUST-1234})
  [tool_call] get_billing_history({customer_id=CUST-1234})
  [tool_call] get_refund_policy({})

Agent: Your Current Plan: Pro ($49/month), Active, Next billing: March 28 2026

Recent Billing History:
- March 1, 2026: $49.00 (INV-3041) - Paid
- February 1, 2026: $49.00 (INV-2987) - Paid
- January 1, 2026: $49.00 (INV-2934) - Paid

Refund Policy: eligible within 30 days, 5-10 business days processing.
What is the reason for your refund request?

---

You: Also I was charged twice
  [tool_call] get_plan_info({customer_id=CUST-1234})
  [tool_call] get_billing_history({customer_id=CUST-1234})
  [tool_call] get_refund_policy({})

Agent: I can see one charge per month. Could you clarify which specific
date shows the duplicate so I can open a refund request for you?

---

You: Where are the log files on Linux?
  [Routing to Technical Specialist]

Agent: On Linux: /var/log/support-platform/agent.log

For verbose output set log_level: debug in config.yaml,
then restart with sudo systemctl restart sp-agent.

---

You: What is the weather today?

Agent: I'm a Technical Support Specialist and can only help with
platform-related technical issues. I can't provide weather information.

---

You: exit
Goodbye!
```

---

## What this conversation demonstrates

- **DJL local semantic search** — embeddings computed locally, no API key needed
- **Correct routing** — API rate limits routed to Technical, refunds to Billing
- **Parallel tool calls** — 3 tools called in a single billing turn
- **Agent switching** — Technical → Billing → Technical → Billing
- **Out-of-scope** — weather handled gracefully by both agents
- **Context preserved** — billing agent remembered CUST-1234 across turns