# Spring Boot + Spring AI (RAG)

A small practice app for a pair-programming session: **how do you answer questions from your own documents with Spring AI?**

This is the Spring AI topic interviewers ask most often: **retrieval-augmented generation (RAG)**. The model does not answer from general knowledge. It first searches a small set of company policy files, then answers from those files only.

Spring Boot **3.5**, Java **21**, Spring AI **1.1.8**. The chat model and the embedding model both run locally in **Ollama**, so the project has no API key.

## The problem this solves

A company wants a chat box for HR policy. If the model answers from memory, it invents numbers. The numbers have to come from the policy documents the company wrote.

## End goal

- Turn policy files into embeddings and store them in a vector store.
- On each question, retrieve the few chunks that are closest to the question.
- Ask the model to answer using only those chunks.
- Return the answer and the file names it came from.

## How it works in this app

```text
POST /api/faq  { "question": "How many vacation days do I get?" }
        |
        v
QuestionAnswerAdvisor
        |
        v
embed the question, search SimpleVectorStore (top 3 chunks)
        |
        v
append those chunks to the prompt
        |
        v
Ollama (llama3.2) writes the answer
        |
        v
{ "answer": "...", "sources": ["leave-policy.txt"] }
```

At startup the app reads `src/main/resources/docs/*.txt`, splits each file into chunks, embeds the chunks with `nomic-embed-text`, and stores them in `SimpleVectorStore`. That store is in memory. It is the right store for a demo. A production app would use the same `VectorStore` interface with pgvector, Redis, or Elasticsearch.

| Piece | What it does here |
|---|---|
| `TextReader` | Reads each policy file |
| `TokenTextSplitter` | Breaks a file into chunks the model can use |
| `EmbeddingModel` (`nomic-embed-text`) | Turns text into a vector |
| `SimpleVectorStore` | Keeps the vectors in memory and finds the closest ones |
| `QuestionAnswerAdvisor` | Searches the store and pastes the chunks into the prompt |
| `ChatClient` | Calls the chat model with a system prompt that forbids guessing |

## Why it is the interview question

Interviewers want to hear the four steps, in order:

1. **Load** documents.
2. **Embed** them and store the vectors.
3. **Retrieve** the closest chunks for the user's question.
4. **Generate** an answer that is grounded in those chunks.

They also want the failure mode: if the documents do not contain the answer, the model should say it does not know.

## Where businesses use this

| Use case | Example |
|---|---|
| Internal policy chat | HR, security, and travel rules |
| Support deflection | Answer from the public help center before opening a ticket |
| Engineering docs | "How do I rotate this credential?" from the runbooks |
| Contract Q&A | Find the clause, then answer, and show the source |

## Trade-offs to mention in an interview

- **The model can still ignore the context.** The system prompt and the advisor text tell it not to, but that is not a guarantee. Show the source files so a person can check.
- **Bad chunks, bad answers.** If the splitter cuts a rule in half, retrieval returns half a rule. Chunk size matters.
- **Embeddings and chat are different models.** This app embeds with `nomic-embed-text` and chats with `llama3.2`. You cannot swap one without re-indexing.
- **In-memory store dies with the process.** `SimpleVectorStore` is for demos. Restarting the app re-embeds the files.
- **Another system to run.** If Ollama is down, the app cannot start, because indexing runs at startup.

## What you need

| Tool | Check it with | Notes |
|---|---|---|
| Java 21 | `java -version` | Spring Boot 3.5 supports 17 to 25. This project is set to 21. |
| Maven | `mvn -version` | Must show `Java version: 21` |
| Docker Desktop | `docker info` | Runs Ollama. The two models need a few GB of disk. |
| Git | `git --version` | Only to upload to GitHub |

On a Mac with Homebrew and several JDKs installed, point Maven at Java 21 in every new terminal window:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

## Run it

Start Ollama, then download the two models once. The first pull is large.

```bash
docker compose up -d
docker compose exec ollama ollama pull llama3.2
docker compose exec ollama ollama pull nomic-embed-text
```

Then start the app:

```bash
mvn spring-boot:run
```

Ask a question the policy files can answer:

```bash
curl -s http://localhost:8080/api/faq \
  -H 'Content-Type: application/json' \
  -d '{"question":"How many vacation days do I get?"}'
```

Ask something the files do not cover. The answer should say it does not know:

```bash
curl -s http://localhost:8080/api/faq \
  -H 'Content-Type: application/json' \
  -d '{"question":"What is the office wifi password?"}'
```

## Other interview questions in this same app

Restart the app after pulling this update. Ollama is already running, so you do not need to pull the models again.

| Endpoint | Interview topic | What it does |
|---|---|---|
| `POST /api/chat` | Plain chat | Asks the model directly. It can answer from general knowledge. |
| `POST /api/faq` | RAG | Searches the policy files, then answers from those files. |
| `POST /api/tickets` | Structured output | Returns a Java object: category, priority, summary. |
| `POST /api/conversations` | Chat memory | Remembers earlier messages that use the same `conversationId`. |
| `POST /api/assistant` | Tool calling | The model calls a Java method to look up a desk. |

```bash
curl -s http://localhost:8080/api/chat \
  -H 'Content-Type: application/json' \
  -d '{"message":"What is Spring Boot in one sentence?"}'

curl -s http://localhost:8080/api/tickets \
  -H 'Content-Type: application/json' \
  -d '{"note":"I need 3 extra vacation days next week"}'

curl -s http://localhost:8080/api/conversations \
  -H 'Content-Type: application/json' \
  -d '{"conversationId":"lalitha-1","message":"My name is Lalitha"}'

curl -s http://localhost:8080/api/conversations \
  -H 'Content-Type: application/json' \
  -d '{"conversationId":"lalitha-1","message":"What is my name?"}'

curl -s http://localhost:8080/api/assistant \
  -H 'Content-Type: application/json' \
  -d '{"question":"Where does Lalitha sit?"}'
```

The desk lookup only knows Lalitha and Kalyani. Those names live in `OfficeTools`, which is a normal Java method. The model calls that method. It does not read them from the policy files.

## Tests

The controller test does not call Ollama. It checks the HTTP contract only.

```bash
mvn test
```
