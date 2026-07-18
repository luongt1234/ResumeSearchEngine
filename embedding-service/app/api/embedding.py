from fastapi import APIRouter, HTTPException, status
from app.schemas.embedding_request import EmbeddingRequest, BatchEmbeddingRequest
from app.schemas.embedding_response import EmbeddingResponse, BatchEmbeddingResponse
from app.services.embedding_service import EmbeddingService
from app.core.model_manager import model_manager
import logging

logger = logging.getLogger(__name__)
router = APIRouter()


@router.post("/embed", response_model=EmbeddingResponse)
def embed(request: EmbeddingRequest):
    """Generate a single embedding vector for the provided text."""
    if not request.text or not request.text.strip():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Field 'text' must not be empty.",
        )

    try:
        embedding = EmbeddingService.embed_text(request.text)
        return EmbeddingResponse(dimension=len(embedding), embedding=embedding)
    except RuntimeError as e:
        logger.error(f"Model not ready — {e}")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Embedding model is not loaded yet. Try again shortly.",
        )
    except Exception as e:
        logger.exception(f"Unexpected error while embedding text: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Internal server error.",
        )


@router.post("/embeds", response_model=BatchEmbeddingResponse)
def embeds(request: BatchEmbeddingRequest):
    """Generate embedding vectors for a list of texts.
    
    Empty strings within the list are rejected individually with a clear
    error message – the whole batch is not discarded because of a single
    bad item.
    """
    if not request.texts:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Field 'texts' must not be an empty list.",
        )

    # Identify which positions have empty / whitespace-only strings
    empty_indices = [i for i, t in enumerate(request.texts) if not t.strip()]
    if empty_indices:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Texts at indices {empty_indices} are empty. All texts must be non-empty.",
        )

    try:
        embeddings = EmbeddingService.embed_texts(request.texts)
        dimension = len(embeddings[0]) if embeddings else model_manager.get_dimension()
        return BatchEmbeddingResponse(dimension=dimension, embeddings=embeddings)
    except RuntimeError as e:
        logger.error(f"Model not ready — {e}")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Embedding model is not loaded yet. Try again shortly.",
        )
    except Exception as e:
        logger.exception(f"Unexpected error while embedding batch: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Internal server error.",
        )


@router.get("/health")
def health():
    """Return service health and model readiness."""
    if model_manager.is_loaded():
        return {"status": "healthy", "model": model_manager.get_model_name()}
    return {"status": "starting", "model": model_manager.get_model_name()}
