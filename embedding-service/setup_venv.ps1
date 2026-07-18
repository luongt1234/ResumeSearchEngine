# ========================
# Embedding Service - Virtual Environment Setup Script
# ========================
#
# Chạy script này để thiết lập môi trường ảo cho embedding-service:
#   .\setup_venv.ps1
#
# Script sẽ:
#   1. Tạo virtual environment trong thư mục ./venv
#   2. Cài torch CPU-only (nhẹ hơn ~2GB so với bản CUDA)
#   3. Cài toàn bộ dependencies còn lại

Write-Host ""
Write-Host "===============================" -ForegroundColor Cyan
Write-Host " Embedding Service - venv Setup" -ForegroundColor Cyan
Write-Host "===============================" -ForegroundColor Cyan
Write-Host ""

# Bước 1: Tạo venv
if (Test-Path ".\venv") {
    Write-Host "[1/4] Virtual environment da ton tai, bo qua buoc tao..." -ForegroundColor Yellow
} else {
    Write-Host "[1/4] Tao virtual environment..." -ForegroundColor Green
    python -m venv venv
}

# Bước 2: Kích hoạt venv
Write-Host "[2/4] Kich hoat venv..." -ForegroundColor Green
. .\venv\Scripts\Activate.ps1

# Bước 3: Cài torch CPU-only trước (tránh tải bản CUDA 2.4GB)
Write-Host "[3/4] Cai torch CPU-only (nhe hon ~2GB)..." -ForegroundColor Green
pip install torch --index-url https://download.pytorch.org/whl/cpu --quiet

# Bước 4: Cài phần còn lại từ requirements.txt
Write-Host "[4/4] Cai cac dependencies con lai..." -ForegroundColor Green
pip install -r requirements.txt --quiet

Write-Host ""
Write-Host "===============================" -ForegroundColor Green
Write-Host " Hoan thanh! De chay service:" -ForegroundColor Green
Write-Host ""
Write-Host "   .\venv\Scripts\Activate.ps1" -ForegroundColor White
Write-Host "   uvicorn app.main:app --host 0.0.0.0 --port 9100" -ForegroundColor White
Write-Host "===============================" -ForegroundColor Green
Write-Host ""
