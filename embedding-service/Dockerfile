FROM python:3.11-slim

WORKDIR /app

# Prevent Python from writing .pyc files inside the container
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

# Install system dependencies
RUN apt-get update && apt-get install -y \
    build-essential \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create a non-root user for security best practices
RUN adduser --disabled-password --gecos '' appuser

# Copy and install Python dependencies as root (before switching user)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application source code
COPY app/ ./app/

# Switch to non-root user
USER appuser

# Expose service port
EXPOSE 9100

# Docker health check — lets orchestrators restart unhealthy containers
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:9100/health || exit 1

# Start the server
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "9100"]
