import asyncio
import os
from contextlib import asynccontextmanager
from fastapi import FastAPI, Request, status
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
import logging

from app.api.embedding import router as embedding_router
from app.core.model_manager import model_manager

# Configure logging — reads LOG_LEVEL from env, defaults to INFO
log_level = os.getenv("LOG_LEVEL", "INFO").upper()
logging.basicConfig(
    level=getattr(logging, log_level, logging.INFO),
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Manage application startup and shutdown lifecycle."""
    logger.info("Starting up Embedding Service...")
    try:
        # load_model() is a blocking CPU/IO operation — run it in a thread
        # pool executor so the Uvicorn event loop stays unblocked.
        loop = asyncio.get_event_loop()
        await loop.run_in_executor(None, model_manager.load_model)
        logger.info("Embedding Service is ready.")
    except Exception as e:
        # A failed model load is unrecoverable — signal the process to exit
        # so Docker / Kubernetes knows to restart the container.
        logger.critical(f"FATAL: Could not load embedding model — {e}")
        raise SystemExit(1)
    yield
    # ── Shutdown ──────────────────────────────────────────────────────────
    logger.info("Shutting down Embedding Service...")


app = FastAPI(
    title="Embedding Service",
    description="Independent Python service for text embedding generation.",
    version="1.0.0",
    lifespan=lifespan,
)


# ── Global exception handlers ─────────────────────────────────────────────────

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """Return 400 with structured errors instead of FastAPI's default 422."""
    return JSONResponse(
        status_code=status.HTTP_400_BAD_REQUEST,
        content={"detail": "Invalid request payload", "errors": exc.errors()},
    )


# ── Routers ───────────────────────────────────────────────────────────────────

app.include_router(embedding_router)


# ── Entrypoint ────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    import uvicorn
    # Allow running directly via `python app/main.py`
    uvicorn.run("app.main:app", host="0.0.0.0", port=9100, reload=False)
