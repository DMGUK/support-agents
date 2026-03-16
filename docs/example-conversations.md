# Example Conversations

These transcripts are real outputs from the system demonstrating
agent routing, dynamic switching, multi-turn context, semantic search,
and out-of-scope handling.

Semantic search is enabled via OpenAI embeddings (text-embedding-3-small).

---

## Conversation 1 — Full system demo
```
You: My HubSpot integration keeps failing with a 401 error
  [Routing to Technical Specialist]

Agent: A 401 Unauthorized error means the HubSpot token has expired or been revoked.

Fix:
1. Go to HubSpot → Settings → Private Apps
2. Find your Support Platform app and regenerate the Access Token
3. Go to Support Platform → Integrations → HubSpot → Edit
4. Replace the old token and click Test Connection
5. You should see a green Connected badge

Also make sure these scopes are enabled:
- crm.objects.contacts.read
- crm.objects.contacts.write
- crm.objects.deals.read
- timeline

---

You: How do I fix ERR-003?

Agent: ERR-003 is a TLS Certificate Error — your Java truststore doesn't
include the platform's certificate authority.

Fix:
1. Download: https://api.support-platform.example.com/ca-bundle.pem
2. Import: keytool -importcert -alias sp-ca -file ca-bundle.pem
         -keystore $JAVA_HOME/lib/security/cacerts
   (default password: changeit)
3. Restart the agent service

This resolves PKIX path building failed and SSL handshake errors.

---

You: Where are the log files on Linux?

Agent: On Linux: /var/log/support-platform/agent.log

For verbose output set log_level: debug in config.yaml,
then restart with sudo systemctl restart sp-agent.

---

You: What are the API rate limits for the Pro plan?

Agent: Pro plan: 120 requests/minute, 50,000 requests/day.
If exceeded, the API returns HTTP 429 with a Retry-After header.

| Plan       | Requests/minute | Requests/day |
|------------|-----------------|--------------|
| Free       | 10              | 500          |
| Pro        | 120             | 50,000       |
| Enterprise | Unlimited       | Unlimited    |

---

You: I want a refund
  [Routing to Billing Specialist]

Agent: I'd be happy to help. Could you please provide your customer ID?

---

You: my customer ID is CUST-1234
  [tool_call] get_plan_info({customer_id=CUST-1234})
  [tool_call] get_billing_history({customer_id=CUST-1234})
  [tool_call] get_refund_policy({})

Agent: Your Current Plan: Pro ($49/month), Active, Next billing: March 28 2026

Recent Billing History:
- March 1, 2026: $49 (Paid)
- February 1, 2026: $49 (Paid)
- January 1, 2026: $49 (Paid)

Refund Policy: eligible within 30 days, 5-10 business days processing.
Your March 1st charge is within the refund window.
Would you like to open a refund case or receive a refund form?

---

You: My HubSpot integration keeps failing with a 401 error
  [Routing to Technical Specialist]

Agent: [same fix as above — token expired or revoked]

---

You: Also I was charged twice
  [Routing to Billing Specialist]
  [tool_call] open_refund_request({reason=Duplicate charge, customer_id=CUST-1234})

Agent: Refund request opened.
Case #REF-17635 — Status: Opened
You will receive a confirmation email within 1 business day.

---

You: What is the weather today?

Agent: I'm a Billing Specialist and can only help with billing-related
questions. I don't have access to weather information.

---

You: my customer ID is CUST-5678
  [tool_call] get_billing_history({customer_id=CUST-5678})
  [tool_call] open_refund_request({reason=Duplicate charge, customer_id=CUST-5678})

Agent: Billing history reviewed. Refund request opened.
Case #REF-92192 — Status: Opened
You will receive a confirmation email within 1 business day.

---

You: My HubSpot integration keeps failing with a 401 error
  [Routing to Technical Specialist]

Agent: [same fix — token expired or revoked]

---

You: What is the weather today?

Agent: I don't have access to weather information in my technical support
documentation. I can only help with platform, integration, billing,
and troubleshooting questions.

---

You: exit
Goodbye!
```

---

## What this conversation demonstrates

- **Semantic search enabled** — OpenAI embeddings retrieve relevant doc chunks by meaning, not just keywords
- **Correct routing of API questions** — "API rate limits for Pro plan" correctly routed to Technical Specialist
- **Billing multi-turn** — customer ID provided as follow-up, agent remembered context
- **Parallel tool calls** — get_plan_info, get_billing_history, get_refund_policy called in one turn
- **Proactive tool use** — billing agent opened refund request automatically when duplicate charge was mentioned
- **Agent switching** — multiple switches between Technical and Billing throughout the conversation
- **Out-of-scope** — weather handled gracefully by both agents staying in character
- **Context preservation** — billing agent remembered CUST-1234 after switching back from Technical