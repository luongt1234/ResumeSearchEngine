# 📄 Resume Search Engine

> **Hệ thống CV ETL & Hybrid Search** — Nền tảng tuyển dụng thông minh cho phép upload, phân tích, và tìm kiếm hồ sơ ứng viên (CV/Resume) với độ chính xác cao bằng cách kết hợp Full-text Search và Semantic Vector Search.

---

## ✨ Tính năng nổi bật

- 🔍 **Hybrid Search**: Kết hợp Elasticsearch (BM25) + Weaviate (Vector DB) để tìm kiếm CV vừa chính xác từ khóa vừa hiểu ngữ nghĩa.
- 📤 **ETL Pipeline tự động**: Upload CV (PDF/Word) → Trích xuất OCR → Chuyển đổi vector → Lưu vào search index qua Apache Kafka.
- 🤖 **AI Resume Parser**: Fine-tune LLM (Qwen2.5-3B) để bóc tách dữ liệu có cấu trúc từ CV thô thành JSON chuẩn hóa.
- 🏢 **Quản lý Job Batches**: Tạo batch tuyển dụng, upload CV ứng viên hàng loạt và tự động scoring.
- 🔐 **Xác thực & Phân quyền**: JWT-based authentication với Spring Security.
- 💻 **Giao diện web hiện đại**: React 19 + TypeScript + Tailwind CSS 4.

---

## 🏗️ Kiến trúc hệ thống

```
┌────────────────────────────────────────────────────────────────┐
│                        Frontend (React)                        │
│          React 19 + TypeScript + Tailwind CSS 4 + Vite         │
└────────────────────────┬───────────────────────────────────────┘
                         │ HTTP
┌────────────────────────▼───────────────────────────────────────┐
│                     API Gateway                                │
│              Spring Cloud Gateway (Port 8080)                  │
└──┬──────────┬──────────┬──────────┬──────────┬────────────────┘
   │          │          │          │          │
   ▼          ▼          ▼          ▼          ▼
Auth     Resume      Job       Search      ETL Worker
Service  Service    Service    Service      Service
  │          │          │          │          │
  │          ▼          │          ▼          ▼
  │        MinIO        │      Weaviate +   Kafka
  │      (File Store)   │    Elasticsearch  (Events)
  ▼                     ▼
MySQL               MySQL

                  ┌──────────────────┐
                  │ Embedding Service │
                  │ Python FastAPI    │
                  │ Qwen3-Embed-0.6B  │
                  └──────────────────┘

              ┌──────────────────────────┐
              │  CV LLM Fine-Tune Project │
              │  Qwen2.5-3B + QLoRA      │
              │  (Kaggle / Colab)        │
              └──────────────────────────┘
```

### 📦 Các thành phần chính

#### Backend — Spring Boot Microservices (`resume-search-engine/`)
> **Java 21 | Spring Boot 3.5 | Spring Cloud 2025**

| Service | Cổng | Mô tả |
|---|---|---|
| `discovery-service` | 8761 | Eureka Service Registry — đăng ký & khám phá service |
| `api-gateway` | 8080 | API Gateway — định tuyến, load balancing, xác thực JWT |
| `auth-service` | — | Đăng ký, đăng nhập, cấp phát JWT |
| `resume-service` | — | CRUD hồ sơ CV, upload file lên MinIO, publish event Kafka |
| `job-service` | — | Quản lý vị trí tuyển dụng và batch ứng viên |
| `etl-worker-service` | — | Worker xử lý nền: OCR CV → Embed → Index vào Elasticsearch & Weaviate |
| `search-service` | — | Hybrid search API kết hợp full-text + vector search |

#### AI/ML — Embedding Service (`embedding-service/`)
> **Python 3.11 | FastAPI | gRPC | SentenceTransformers**

- Model: `Qwen/Qwen3-Embedding-0.6B` (1024-dim vectors)
- Giao tiếp: gRPC với các Java services
- Tự động phát hiện GPU/CPU
- Singleton model loading, thread-safe

#### AI/ML — LLM Fine-Tuning (`cv-llm-finetune-project/`)
> **Python | Unsloth | QLoRA | Qwen2.5-3B-Instruct**

- Fine-tune mô hình LLM để **tự động phân tích và bóc tách thông tin CV** từ text OCR thành JSON có cấu trúc.
- Huấn luyện trên Kaggle/Colab với QLoRA để tiết kiệm VRAM.
- Export sang định dạng GGUF để inference cục bộ.

#### Frontend (`frontend/`)
> **React 19 | TypeScript | Tailwind CSS 4 | Vite**

Các trang chính:
- `/login` — Đăng nhập / đăng ký
- `/dashboard` — Tổng quan hệ thống
- `/upload` — Upload CV ứng viên
- `/search` — Tìm kiếm CV (Hybrid Search)
- `/batches` — Quản lý batch tuyển dụng
- `/batches/:id/scoring` — Xem kết quả scoring ứng viên

#### Infrastructure (`docker-compose.yml`)

| Service | Cổng | Mô tả |
|---|---|---|
| **MinIO** | 9000 / 9001 | Object Storage lưu trữ file CV (S3-compatible) |
| **Apache Kafka** | 9092 | Message Broker giao tiếp bất đồng bộ |
| **Weaviate** | 8088 | Vector Database cho Semantic Search |
| **Elasticsearch** | 9200 | Full-text Search Engine |
| **MySQL** | 3306 | Relational Database |

---

## 🚀 Hướng dẫn cài đặt & khởi chạy

### Yêu cầu hệ thống

| Công cụ | Phiên bản |
|---|---|
| [Docker](https://www.docker.com/) & Docker Compose | Latest |
| [Java JDK](https://jdk.java.net/21/) | 21+ |
| [Python](https://www.python.org/) | 3.11+ |
| [Node.js](https://nodejs.org/) | 20+ |
| [Maven](https://maven.apache.org/) | 3.9+ (hoặc dùng `mvnw` có sẵn) |

---

### Bước 1 — Khởi chạy Infrastructure

```bash
# Tại thư mục gốc dự án
docker compose up -d
```

Kiểm tra các service đã chạy:

```
MinIO Console  →  http://localhost:9001
Weaviate       →  http://localhost:8088
Elasticsearch  →  http://localhost:9200
Kafka          →  localhost:9092
MySQL          →  localhost:3306
```

---

### Bước 2 — Khởi chạy Backend (Spring Boot Microservices)

```bash
cd resume-search-engine

# Bước 2a: Khởi chạy Discovery Service trước
./mvnw -pl discovery-service spring-boot:run

# Bước 2b: Khởi chạy các service còn lại (theo thứ tự hoặc chạy song song)
./mvnw -pl api-gateway spring-boot:run
./mvnw -pl auth-service spring-boot:run
./mvnw -pl resume-service spring-boot:run
./mvnw -pl job-service spring-boot:run
./mvnw -pl etl-worker-service spring-boot:run
./mvnw -pl search-service spring-boot:run
```

> 💡 **Khuyến nghị**: Sử dụng IDE (IntelliJ IDEA) để chạy song song nhiều service cùng lúc.

---

### Bước 3 — Khởi chạy Embedding Service (Python)

```bash
cd embedding-service

# Tạo và kích hoạt virtual environment
python -m venv venv
# Windows:
venv\Scripts\activate
# Linux/macOS:
source venv/bin/activate

# Cài đặt dependencies
pip install -r requirements.txt

# Chạy gRPC server
python -m app.main
```

Hoặc chạy bằng Docker:

```bash
cd embedding-service
docker build -t embedding-service .
docker run -p 9100:9100 embedding-service
```

---

### Bước 4 — Khởi chạy Frontend

```bash
cd frontend
npm install
npm run dev
```

Ứng dụng sẽ chạy tại: **http://localhost:5173**

---

## 🤖 AI Fine-Tuning — CV Resume Parser

Module `cv-llm-finetune-project/` chứa toàn bộ pipeline để fine-tune LLM phân tích CV.

### Quy trình

```
Raw CV (PDF/Image)
       │
       ▼
  OCR Extraction          ← script: 00_parse_random_1000_offline.py
       │
       ▼
ShareGPT Data Format      ← script: 01_prepare_sharegpt_data.py
       │
       ▼
QLoRA Fine-tuning         ← notebook: qwen25_qlora_resume_parser.ipynb
(Kaggle / Google Colab)              unsloth_qlora_training.ipynb
       │
       ├── LoRA Adapters → test: python scripts/02_test_local_model.py --mode lora
       └── GGUF Export   → test: python scripts/02_test_local_model.py --mode gguf
```

### Cài đặt môi trường Fine-Tune

```bash
cd cv-llm-finetune-project
python -m venv venv
venv\Scripts\activate  # Windows
pip install -r requirements.txt
```

### Test model sau khi train

```bash
# Test với GGUF model
python scripts/02_test_local_model.py --mode gguf --model-path exports/gguf_model/model.gguf

# Test với LoRA adapters
python scripts/02_test_local_model.py --mode lora --model-path exports/lora_adapters
```

---

## 📁 Cấu trúc thư mục

```
ResumeSearchEngine/
│
├── 📂 resume-search-engine/        # Spring Boot Microservices (Java 21)
│   ├── api-gateway/                # API Gateway (Spring Cloud Gateway)
│   ├── auth-service/               # Authentication & Authorization
│   ├── discovery-service/          # Eureka Service Registry
│   ├── etl-worker-service/         # ETL Background Worker
│   ├── job-service/                # Job & Batch Management
│   ├── resume-service/             # Resume CRUD & File Upload
│   ├── search-service/             # Hybrid Search API
│   └── pom.xml                     # Parent Maven POM
│
├── 📂 embedding-service/           # Python AI Service (FastAPI + gRPC)
│   ├── app/
│   │   ├── api/                    # REST API endpoints
│   │   ├── core/                   # Configuration
│   │   ├── services/               # Embedding business logic
│   │   ├── schemas/                # Pydantic models
│   │   ├── grpc_server.py          # gRPC server
│   │   └── main.py                 # Entry point
│   ├── Dockerfile
│   └── requirements.txt
│
├── 📂 cv-llm-finetune-project/     # LLM Fine-Tuning Pipeline
│   ├── scripts/
│   │   ├── 00_parse_random_1000_offline.py   # OCR & CV parsing
│   │   ├── 01_prepare_sharegpt_data.py       # Dataset preparation
│   │   └── 02_test_local_model.py            # Model inference test
│   ├── notebooks/
│   │   ├── qwen25_qlora_resume_parser.ipynb  # Main training notebook
│   │   └── unsloth_qlora_training.ipynb      # Unsloth QLoRA training
│   ├── data/
│   │   ├── raw/                    # Raw OCR & parsed data
│   │   └── processed/              # ShareGPT training dataset
│   └── exports/                    # Trained model outputs (GGUF / LoRA)
│
├── 📂 frontend/                    # React Web App (TypeScript)
│   ├── src/
│   │   ├── components/             # Reusable UI components
│   │   ├── pages/                  # Application pages
│   │   ├── services/               # API client services
│   │   ├── context/                # React Context (Auth, etc.)
│   │   └── lib/                    # Utilities
│   ├── package.json
│   └── vite.config.ts
│
├── 📂 proto/                       # Protocol Buffers definitions
├── 📂 tessdata/                    # Tesseract OCR language data
├── docker-compose.yml              # Infrastructure containers
├── kafka.env                       # Kafka configuration
└── minio.env                       # MinIO configuration
```

---

## 🔄 Luồng xử lý dữ liệu (Data Flow)

### Upload & Index CV

```
User Upload CV (PDF/Word)
        │
        ▼
   resume-service  ──→  MinIO (lưu file)
        │
        ▼  (Kafka Event)
  etl-worker-service
        │
        ├──→ OCR / Text Extraction (Apache Tika / Tesseract)
        │
        ├──→ embedding-service (gRPC) → Qwen3-Embed-0.6B → vector
        │
        ├──→ Weaviate (lưu vector)
        │
        └──→ Elasticsearch (lưu text)
```

### Tìm kiếm CV

```
User Search Query
        │
        ▼
   search-service
        │
        ├──→ Elasticsearch (BM25 full-text search)
        │
        ├──→ embedding-service → query vector
        │         └──→ Weaviate (vector similarity search)
        │
        └──→ Score Fusion & Ranking → Kết quả trả về
```

---

## 🛠️ Công nghệ sử dụng

| Layer | Công nghệ |
|---|---|
| **Backend** | Java 21, Spring Boot 3.5, Spring Cloud 2025 |
| **AI/Embedding** | Python 3.11, FastAPI, gRPC, SentenceTransformers, Qwen3-Embedding-0.6B |
| **LLM Fine-Tuning** | Qwen2.5-3B-Instruct, QLoRA, Unsloth, PEFT, TRL |
| **Frontend** | React 19, TypeScript, Tailwind CSS 4, Vite, React Router |
| **Search** | Elasticsearch 8.11, Weaviate 1.24 |
| **Messaging** | Apache Kafka |
| **Storage** | MinIO (S3-compatible), MySQL 8.0 |
| **Infrastructure** | Docker, Docker Compose |

---

## 📝 License

MIT License — xem file [LICENSE](LICENSE) để biết thêm chi tiết.
