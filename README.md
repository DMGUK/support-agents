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
Relevant paragraphs are retrieved via semantic similarity (cosine similarity on ONNX embeddings)
and injected into the system prompt. Claude is strictly instructed to answer only from those
excerpts — no hallucination allowed.

### Agent B — Billing Specialist
Handles billing questions using tool-calling. Claude decides which backend function
to invoke, the Java code executes it, and the result is fed back for a final response.
Supports parallel tool calls in a single turn.

### Router
Classifies every user message using Claude with a strict one-word output prompt.
Uses the last 4 messages as context to correctly handle follow-up messages like
providing a customer ID after being asked for one.

### Semantic Search (this branch)
Uses DJL (Deep Java Library) with ONNX Runtime to run the
`all-MiniLM-L6-v2` sentence transformer model locally.
No external API key required — the model is downloaded once (~90MB)
and cached at `~/.cache/support-agents/`.

---

## Tech Stack

- **Language:** Java 21
- **LLM:** Claude Sonnet (Anthropic API)
- **Embeddings:** ONNX Runtime + DJL HuggingFace tokenizers (local, no API key)
- **HTTP client:** OkHttp 4.12
- **JSON:** Jackson 2.17
- **Build:** Maven

No agentic frameworks used. All orchestration is implemented manually.

---

## Branches

| Branch | Embeddings | Requires |
|--------|-----------|---------|
| `main` | OpenAI `text-embedding-3-small` | `OPENAI_API_KEY` |
| `feature/djl-local-embeddings` | DJL local ONNX model | Nothing extra |

Both branches fall back to keyword search if the embedding provider is unavailable.

---

## Project Structure
```
demo/
├── docs/
│   ├── setup-guide.md
│   ├── api-reference.md
│   ├── troubleshooting.md
│   └── hubspot-integration.md
├── src/main/java/com/support/
│   ├── Main.java
│   ├── model/
│   │   ├── Message.java
│   │   ├── AgentType.java
│   │   └── ConversationSession.java
│   ├── llm/
│   │   └── ClaudeClient.java
│   ├── router/
│   │   └── AgentRouter.java
│   ├── rag/
│   │   ├── DocumentChunk.java
│   │   ├── DocumentLoader.java
│   │   ├── DocumentRetriever.java       ← keyword fallback
│   │   ├── DJLEmbeddingClient.java      ← ONNX local embeddings
│   │   └── DJLSemanticDocumentRetriever.java
│   ├── agents/
│   │   ├── TechnicalAgent.java
│   │   └── BillingAgent.java
│   └── tools/
│       └── BillingTools.java
└── pom.xml
```

---

## Requirements

- Java 21+
- Maven 3.8+
- Anthropic API key
- Internet connection on first run (downloads ~90MB ONNX model, then cached)

---

## Setup & Running

### 1. Clone the repository
```bash
git clone https://github.com/DMGUK/support-agents.git
cd support-agents/demo
git checkout feature/djl-local-embeddings
```

### 2. Set your Anthropic API key

**Windows (permanent):**
Go to Start → Search "Environment Variables" → User variables → New
- Name: `ANTHROPIC_API_KEY`
- Value: `sk-ant-...`

**Linux / macOS:**
```bash
export ANTHROPIC_API_KEY="sk-ant-..."
```

### 3. Build
```bash
mvn clean package
```

### 4. Run
```bash
java -jar target/support-agents.jar
```

On first run the ONNX model and tokenizer are downloaded and cached.
Subsequent runs start instantly.

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

---

## Documentation Files

| File | Topics |
|------|--------|
| `setup-guide.md` | Installation, config.yaml, health check |
| `api-reference.md` | Endpoints, auth, rate limits, error codes |
| `troubleshooting.md` | ERR-001 through ERR-005, log locations |
| `hubspot-integration.md` | OAuth setup, field mapping, common errors |