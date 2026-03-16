# Conversational AI Support Agents

A multi-agent conversational support system built in Java using the Anthropic Claude API.
The system routes user messages to one of two specialised AI agents — a Technical Specialist
and a Billing Specialist — with automatic routing, dynamic switching, and multi-turn context.

---

## Architecture
```
User message
      │
      ▼
AgentRouter          ← classifies intent using Claude (TECHNICAL / BILLING / OUT_OF_SCOPE)
      │
      ├──► TechnicalAgent   ← RAG pipeline: retrieves relevant doc chunks → grounded answer
      ├──► BillingAgent     ← tool-calling: invokes mock billing functions → final answer
      └──► Out-of-scope     ← hardcoded polite reply
```

### Agent A — Technical Specialist
Answers questions using a small set of local documentation files.
Relevant paragraphs are retrieved via keyword overlap scoring and injected into the
system prompt. Claude is strictly instructed to answer only from those excerpts —
no hallucination allowed.

### Agent B — Billing Specialist
Handles billing questions using tool-calling. Claude decides which backend function
to invoke, the Java code executes it, and the result is fed back for a final response.
Supports parallel tool calls in a single turn.

### Router
Classifies every user message using Claude with a strict one-word output prompt.
Uses the last 4 messages as context to correctly handle follow-up messages like
providing a customer ID after being asked for one.

---

## Tech Stack

- **Language:** Java 21
- **LLM:** Claude Sonnet (Anthropic API)
- **HTTP client:** OkHttp 4.12
- **JSON:** Jackson 2.17
- **Build:** Maven

No agentic frameworks used. All orchestration is implemented manually.

---

## Project Structure
```
demo/
├── docs/                          # Technical documentation (4 files)
│   ├── setup-guide.md
│   ├── api-reference.md
│   ├── troubleshooting.md
│   └── hubspot-integration.md
├── src/main/java/com/support/
│   ├── Main.java                  # Entry point and chat loop
│   ├── model/
│   │   ├── Message.java
│   │   ├── AgentType.java
│   │   └── ConversationSession.java
│   ├── llm/
│   │   └── ClaudeClient.java      # Anthropic API wrapper
│   ├── router/
│   │   └── AgentRouter.java       # LLM-based intent classifier
│   ├── rag/
│   │   ├── DocumentChunk.java
│   │   ├── DocumentLoader.java
│   │   └── DocumentRetriever.java # Keyword overlap scorer
│   ├── agents/
│   │   ├── TechnicalAgent.java
│   │   └── BillingAgent.java
│   └── tools/
│       └── BillingTools.java      # Mock billing backend (5 functions)
└── pom.xml
```

---

## Requirements

- Java 21+
- Maven 3.8+
- Anthropic API key

---

## Setup & Running

### 1. Clone the repository
```bash
git clone https://github.com/DMGUK/support-agents.git
cd support-agents/demo
```

### 2. Set your API key

**Linux / macOS:**
```bash
export ANTHROPIC_API_KEY="sk-ant-..."
```

**Windows (PowerShell):**
```powershell
$env:ANTHROPIC_API_KEY="sk-ant-..."
```

**Windows (permanent):**
Go to Start → Search "Environment Variables" → User variables → New
- Name: `ANTHROPIC_API_KEY`
- Value: `sk-ant-...`

### 2b. Set your OpenAI API key (optional — enables semantic search)

The system uses OpenAI embeddings for semantic search in the Technical Specialist agent.
If not set, it falls back to keyword search automatically.

**Windows (permanent):**
Go to Start → Search "Environment Variables" → User variables → New
- Name: `OPENAI_API_KEY`
- Value: `sk-...`

Get a key at: https://platform.openai.com/api-keys

### 3. Build
```bash
mvn clean package
```

### 4. Run
```bash
java -jar target/support-agents.jar
```

Type `exit` or `quit` to end the session.

---

## Billing Tools

| Tool | Description |
|------|-------------|
| `get_plan_info` | Current plan, price, billing cycle, next charge |
| `get_billing_history` | Last 3 invoices |
| `open_refund_request` | Opens a refund case, returns case ID |
| `send_refund_form` | Sends refund form to customer email |
| `get_refund_policy` | Eligibility, timelines, exceptions |

All tools return mock data. Replace `BillingTools.java` implementations for real DB/API calls.

---

## Documentation Files

| File | Topics |
|------|--------|
| `setup-guide.md` | Installation, config.yaml, health check |
| `api-reference.md` | Endpoints, auth, rate limits, error codes |
| `troubleshooting.md` | ERR-001 through ERR-005, log locations |
| `hubspot-integration.md` | OAuth setup, field mapping, common errors |