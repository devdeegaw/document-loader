# Document Loader

A **Spring Boot & Spring AI**–based pipeline for ingesting and processing documents using Google GenAI embeddings and storing semantic vectors in a **Qdrant** vector database.

---

## 🚀 Overview

`document-loader` is designed to automatically read, split, embed, and store documents (PDF, DOCX, TXT, etc.) as AI‑searchable chunks.  
It leverages **Apache Tika** for document parsing and **Spring Cloud Function** to orchestrate a reactive ETL pipeline.

The project demonstrates:
- Integration with **Google Gemini / GenAI** models for text embedding and chat.
- Use of **Qdrant** as a vector store via gRPC.
- Automatic document loading and function‑based processing flows.
- A clean, modular **Spring Boot 3.5** configuration.

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
