# Embedding Service

Independent Python Service for Text Embeddings using FastAPI and SentenceTransformers.
This service exposes endpoints for text embedding generation without containing any business logic or external DB connections.

## Architecture & Technology
- Python 3.11
- FastAPI + Uvicorn
- SentenceTransformers + Torch
- Clean Architecture principles

## Features
- Provides REST API for text embedding generation.
- Uses `Qwen/Qwen3-Embedding-0.6B`.
- Model is loaded once globally on startup (Singleton pattern).
- Thread-safe model manager.
- Auto-detects GPU/CPU without hardcoding CUDA.
- Graceful exception handling and validation (HTTP 400, 503).

## Setup & Run Locally

1. **Create virtual environment**:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```
2. **Install dependencies**:
   ```bash
   pip install -r requirements.txt
   ```
3. **Run the service**:
   ```bash
   uvicorn app.main:app --host 0.0.0.0 --port 9100
   ```

## Run with Docker

1. **Build image**:
   ```bash
   docker build -t embedding-service .
   ```
2. **Run container**:
   ```bash
   docker run -p 9100:9100 embedding-service
   ```

## API Usage

### `POST /embed`
**Request:**
```json
{
    "text": "Backend Java Spring Boot Kafka Docker"
}
```

**Response:**
```json
{
    "dimension": 1024,
    "embedding": [0.0123, -0.0456, ...]
}
```

### `POST /embeds`
**Request:**
```json
{
    "texts": ["Backend", "Java Spring Boot"]
}
```

**Response:**
```json
{
    "dimension": 1024,
    "embeddings": [
        [0.0123, ...],
        [0.0456, ...]
    ]
}
```

### `GET /health`
**Response:**
```json
{
    "status": "healthy",
    "model": "Qwen/Qwen3-Embedding-0.6B"
}
```
