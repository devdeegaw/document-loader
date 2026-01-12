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
