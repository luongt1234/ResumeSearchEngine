import logging
import torch
import threading
from sentence_transformers import SentenceTransformer

logger = logging.getLogger(__name__)


class ModelManager:
    """Thread-safe Singleton that owns the embedding model lifecycle."""

    _instance = None
    _class_lock = threading.Lock()  # Guards instance creation only

    def __new__(cls):
        with cls._class_lock:
            if cls._instance is None:
                instance = super().__new__(cls)
                # Initialise all state as instance attributes inside __new__
                # so they are NEVER shared as class-level attributes.
                instance._model: SentenceTransformer | None = None
                instance._model_name: str = "Qwen/Qwen3-Embedding-0.6B"
                instance._load_lock = threading.Lock()  # Guards model loading only
                cls._instance = instance
        return cls._instance

    def load_model(self) -> None:
        """Load the model exactly once, blocking & thread-safe.
        
        Safe to call from multiple threads simultaneously – only the first
        caller will actually perform the download/load; the rest will wait
        and return immediately once the model is ready.
        """
        with self._load_lock:
            if self._model is not None:
                return  # Already loaded

            logger.info(f"Loading model '{self._model_name}'...")
            device = "cuda" if torch.cuda.is_available() else "cpu"
            logger.info(f"Using device: {device}")

            # trust_remote_code=True is required for Qwen3 custom pooling logic
            self._model = SentenceTransformer(
                self._model_name,
                device=device,
                trust_remote_code=True,
            )
            logger.info(f"Model '{self._model_name}' loaded successfully on {device}.")

    def get_model(self) -> SentenceTransformer:
        """Return the loaded model, raises RuntimeError if not yet loaded."""
        if self._model is None:
            raise RuntimeError("Model is not loaded yet. Service may still be starting up.")
        return self._model

    def is_loaded(self) -> bool:
        """Return True if the model has been loaded successfully."""
        return self._model is not None

    def get_dimension(self) -> int:
        """Query the actual embedding dimension from the loaded model."""
        return self.get_model().get_sentence_embedding_dimension()

    def get_model_name(self) -> str:
        return self._model_name


# Module-level singleton instance
model_manager = ModelManager()
