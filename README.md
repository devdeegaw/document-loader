# Document Loader

A **Spring Boot & Spring AI**–based pipeline for ingesting and processing code files using **Ollama** embeddings and storing semantic vectors in a **Qdrant** vector database.

---

## 🚀 Overview

`document-loader` is designed to automatically read, split, embed, and store code files from any directory (recursively scanning subdirectories) as AI‑searchable chunks.  
It leverages **ApacheTika** for document parsing and **SpringCloudFunction** to orchestrate a reactive ETL pipeline.

The project demonstrates:
- Integration with **Ollama** models for text embedding (bge-m3) and chat (llama3.1:8b).
- Use of **Qdrant** as a vector store via gRPC.
- Automatic code file loading with recursive directory scanning.
- A clean, modular **SpringBoot 3.5** configuration.

---

## 🧩 Architecture

```
CodeFileSupplier → documentReader → splitter → titleDeterminer → vectorStoreConsumer
   (Flux<byte[]>)   (Flux<Document>)  (Flux<List<Document>>)  (Flux<List<Document>>)  (stores in Qdrant)
```

---

## 📋 Prerequisites

Before setting up the project, ensure you have the following installed:

1. **Java 21** - [Download](https://adoptium.net/)
2. **Docker Desktop** - [Download](https://www.docker.com/products/docker-desktop/)
3. **Git** - [Download](https://git-scm.com/downloads)
4. **Ollama** - [Download](https://ollama.ai/download)

---

## 🛠️ Local Setup Instructions

### Step 1: Install Ollama and Pull Required Models

```bash
# Install Ollama (macOS)
brew install ollama

# Or download from https://ollama.ai/download

# Start Ollama service
ollama serve

# In a new terminal, pull required models
ollama pull bge-m3        # Embedding model
ollama pull llama3.1:8b   # Chat model
```

### Step 2: Clone the Repository

```bash
git clone <your-repository-url>
cd document-loader
```

### Step 3: Start Qdrant Vector Database

The project uses Docker Compose to run Qdrant:

```bash
# Start Qdrant container
docker-compose up -d

# Verify Qdrant is running
curl http://localhost:6333/collections
```

### Step 4: Configure the Application

Edit `src/main/resources/application.yml` to set your code directory:

```yaml
file:
  supplier:
    directory: /path/to/your/code/directory  # Change this to your target directory
    extensions: java,py,js,ts,cpp,c,go,rs,kt,scala,rb,php,cs,swift,yml,yaml,xml,json,md
```

### Step 5: Build the Project

```bash
# Using Gradle wrapper (recommended)
./gradlew clean build

# On Windows
gradlew.bat clean build
```

### Step 6: Run the Application

```bash
# Using Gradle
./gradlew bootRun

# Or run the JAR directly
java -jar build/libs/document-loader-0.0.1-SNAPSHOT.jar
```

The application will:
1. Start on `http://localhost:8080`
2. Automatically scan the configured directory recursively
3. Process all code files matching the extensions
4. Create embeddings using Ollama (bge-m3)
5. Store vectors in Qdrant

---

## 🔍 Testing the Search API

### Search Query Endpoint

```bash
curl -X POST http://localhost:8080/api/search/query \
  -H "Content-Type: application/json" \
  -d '{"query": "Write jira stories extracted from given code base in Gherkins format"}'
```

### Example Response

```json
"Here are the Jira user stories extracted from the given code base in Gherkin format:

*Story 1:* As a developer, I want to configure Spring Boot application with Function Catalog for processing documents.
..."
```

---

## 📁 Project Structure

```
document-loader/
├── src/main/java/com/rag/documentloader/
│   ├── configuration/
│   │   ├── CodeFileSupplier.java          # Recursively scans directories for code files
│   │   └── DocumentLoaderPipeLine.java    # ETL pipeline configuration
│   ├── controller/
│   │   └── SearchController.java          # REST API for semantic search
│   ├── model/
│   │   └── DocumentTitle.java             # Document metadata model
│   └── DocumentLoaderApplication.java     # Main application entry point
├── src/main/resources/
│   ├── application.yml                    # Application configuration
│   └── templates/
│       └── nameOfTheDocument.st           # LLM prompt template
├── compose.yaml                           # Docker Compose for Qdrant
└── build.gradle                           # Gradle build configuration
```

---

## ⚙️ Configuration

### Key Configuration Properties

| Property | Description | Default |
|----------|-------------|----------|
| `file.supplier.directory` | Root directory to scan for code files | `/path/to/code` |
| `file.supplier.extensions` | Comma-separated file extensions | `java,py,js,ts,...` |
| `spring.ai.ollama.base-url` | Ollama API endpoint | `http://localhost:11434` |
| `spring.ai.ollama.embedding.model` | Embedding model name | `bge-m3` |
| `spring.ai.ollama.chat.model` | Chat model name | `llama3.1:8b` |
| `spring.ai.vectorstore.qdrant.host` | Qdrant host | `localhost` |
| `spring.ai.vectorstore.qdrant.port` | Qdrant gRPC port | `6334` |
| `spring.ai.vectorstore.qdrant.collection-name` | Collection name | `DocumentsMarathi` |

---

## 🐛 Troubleshooting

### Issue: "Connection refused" to Ollama

```bash
# Ensure Ollama is running
ollama serve

# Check if models are available
ollama list
```

### Issue: "Connection refused" to Qdrant

```bash
# Check if Qdrant container is running
docker ps | grep qdrant

# Restart Qdrant
docker-compose restart
```

### Issue: "Port 8080 already in use"

Change the port in `application.yml`:

```yaml
server:
  port: 8081
```

### Issue: SSH connection refused (Git)

Use HTTPS instead:

```bash
git remote set-url origin https://github.com/username/repo.git
```

---

## 🚀 How It Works

1. **CodeFileSupplier** recursively scans the configured directory for code files
2. **documentReader** parses files using Apache Tika
3. **splitter** breaks documents into chunks using TokenTextSplitter
4. **titleDeterminer** uses LLM to generate meaningful titles
5. **vectorStoreConsumer** creates embeddings and stores in Qdrant
6. **SearchController** provides REST API for semantic search with RAG

---

## 📝 Supported File Types

By default, the following code file extensions are supported:
- **Languages**: java, py, js, ts, cpp, c, go, rs, kt, scala, rb, php, cs, swift
- **Config**: yml, yaml, xml, json
- **Docs**: md

You can customize this in `application.yml` under `file.supplier.extensions`.

---

## 🤝 Contributing

Feel free to submit issues and pull requests!

---

## 📄 License

This project is open source and available under the MIT License.
