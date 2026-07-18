from pydantic import BaseModel, Field

class EmbeddingRequest(BaseModel):
    text: str = Field(..., description="Text to embed")

class BatchEmbeddingRequest(BaseModel):
    texts: list[str] = Field(..., description="List of texts to embed")
