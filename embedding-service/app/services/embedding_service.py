from app.core.model_manager import model_manager
from typing import List
import logging

logger = logging.getLogger(__name__)

class EmbeddingService:
    @staticmethod
    def embed_text(text: str) -> List[float]:
        model = model_manager.get_model()
        # encode handles tokenization and inference. Returns numpy array.
        embedding = model.encode(text, normalize_embeddings=True)
        return embedding.tolist()
        
    @staticmethod
    def embed_texts(texts: List[str]) -> List[List[float]]:
        model = model_manager.get_model()
        embeddings = model.encode(texts, normalize_embeddings=True)
        return embeddings.tolist()
