from pydantic import BaseModel
from typing import List

class EmbeddingResponse(BaseModel):
    dimension: int
    embedding: List[float]

class BatchEmbeddingResponse(BaseModel):
    dimension: int
    embeddings: List[List[float]]
