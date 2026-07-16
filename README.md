# Resume Search Engine

Hệ thống **CV ETL & Hybrid Search** (Trích xuất, biến đổi, tải và Tìm kiếm kết hợp cho Hồ sơ ứng viên). Dự án này cung cấp các vi dịch vụ (microservices) để quản lý, xử lý và tìm kiếm hồ sơ ứng viên (CV/Resume) một cách hiệu quả sử dụng các công nghệ tìm kiếm tiên tiến.

## 🏗 Kiến trúc Hệ thống

Dự án được thiết kế theo kiến trúc Microservices, bao gồm các thành phần chính:

### Backend (Spring Boot & Spring Cloud)
Được xây dựng trên nền tảng **Java 21** và **Spring Boot 3.5**.
- **`api-gateway`**: Cổng API Gateway, định tuyến các yêu cầu từ client đến các dịch vụ tương ứng.
- **`auth-service`**: Dịch vụ xác thực và phân quyền người dùng.
- **`resume-service`**: Quản lý thông tin và tệp hồ sơ của ứng viên.
- **`job-service`**: Quản lý thông tin các vị trí công việc/tuyển dụng.
- **`etl-worker-service`**: Xử lý ngầm (background jobs) để trích xuất dữ liệu từ CV (PDF/Word), biến đổi và chuẩn bị dữ liệu cho các công cụ tìm kiếm.
- **`search-service`**: Cung cấp API tìm kiếm Hybrid (kết hợp giữa tìm kiếm từ khóa truyền thống và tìm kiếm theo ngữ nghĩa - vector search).

### Frontend (React & Vite)
- Nằm trong thư mục `frontend/`.
- Được xây dựng với **React 19**, **TypeScript**, và **Tailwind CSS 4**.
- Sử dụng **Vite** để build và tối ưu hóa hiệu suất.

### Infrastructure (Docker Compose)
Hệ thống sử dụng các công nghệ hạ tầng hiện đại, được cấu hình sẵn trong `docker-compose.yml`:
- **MinIO**: Hệ thống lưu trữ Object Storage (tương thích Amazon S3) để lưu trữ các file CV.
- **Apache Kafka**: Message Broker dùng để giao tiếp bất đồng bộ giữa các microservices (đặc biệt trong quá trình ETL).
- **Weaviate**: Cơ sở dữ liệu Vector (Vector Database) phục vụ tìm kiếm theo ngữ nghĩa (Semantic Search).
- **Elasticsearch**: Công cụ tìm kiếm văn bản mạnh mẽ (Full-text Search).

## 🚀 Yêu cầu hệ thống (Prerequisites)
- [Docker](https://www.docker.com/) & Docker Compose
- [Java 21](https://jdk.java.net/21/)
- [Maven](https://maven.apache.org/) (hoặc sử dụng `mvnw` có sẵn trong dự án)
- [Node.js](https://nodejs.org/) (phiên bản 20+ khuyến nghị)

## 🛠 Hướng dẫn Cài đặt & Khởi chạy

### 1. Khởi chạy Hạ tầng (Infrastructure)
Mở terminal tại thư mục gốc của dự án và chạy các container:
```bash
docker-compose up -d
```
Các dịch vụ sẽ chạy tại:
- MinIO Console: http://localhost:9001 (API tại 9000)
- Weaviate: http://localhost:8088
- Elasticsearch: http://localhost:9200
- Kafka: localhost:9092

### 2. Khởi chạy Backend (Microservices)
Bạn cần khởi chạy các dịch vụ Spring Boot theo thứ tự (hoặc cấu hình chúng trên IDE của bạn).
Đi vào thư mục `resume-search-engine`:
```bash
cd resume-search-engine
```
Và sử dụng Maven wrapper để build/chạy từng dịch vụ, ví dụ:
```bash
./mvnw -pl api-gateway spring-boot:run
./mvnw -pl auth-service spring-boot:run
# Tương tự cho các dịch vụ khác...
```

### 3. Khởi chạy Frontend
Đi vào thư mục `frontend/`, cài đặt các phụ thuộc và chạy ứng dụng React:
```bash
cd frontend
npm install
npm run dev
```
Ứng dụng Frontend sẽ có sẵn tại địa chỉ do Vite cung cấp (thường là `http://localhost:5173`).

## 📁 Cấu trúc thư mục dự án
```text
ResumeSearchEngine/
├── frontend/                 # Ứng dụng ReactJS UI
├── resume-search-engine/     # Các Spring Boot Microservices
│   ├── api-gateway/
│   ├── auth-service/
│   ├── etl-worker-service/
│   ├── job-service/
│   ├── resume-service/
│   ├── search-service/
│   └── pom.xml               # Parent POM
├── docker-compose.yml        # Cấu hình Docker cho hạ tầng
└── ...
```

## 📝 License
[Tùy chọn: Thêm thông tin License nếu cần]
