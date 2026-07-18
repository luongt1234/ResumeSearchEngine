import grpc
from concurrent import futures
import logging
import time

from app.core.model_manager import ModelManager
from app import embedding_pb2
from app import embedding_pb2_grpc
from grpc_health.v1 import health
from grpc_health.v1 import health_pb2
from grpc_health.v1 import health_pb2_grpc

logger = logging.getLogger(__name__)

class EmbeddingServiceServicer(embedding_pb2_grpc.EmbeddingServiceServicer):
    def __init__(self, model_manager: ModelManager):
        self.model_manager = model_manager

    def GenerateEmbedding(self, request, context):
        if not request.text:
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details('Text payload cannot be empty')
            return embedding_pb2.EmbeddingResponse()

        try:
            vector = self.model_manager.embed(request.text)
            return embedding_pb2.EmbeddingResponse(
                dimension=len(vector),
                embedding=vector
            )
        except RuntimeError as e:
            context.set_code(grpc.StatusCode.UNAVAILABLE)
            context.set_details(str(e))
            return embedding_pb2.EmbeddingResponse()
        except Exception as e:
            logger.exception("Error generating embedding")
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(f"Internal error: {str(e)}")
            return embedding_pb2.EmbeddingResponse()

    def GenerateEmbeddings(self, request, context):
        if not request.texts:
            context.set_code(grpc.StatusCode.INVALID_ARGUMENT)
            context.set_details('Texts array cannot be empty')
            return embedding_pb2.EmbeddingBatchResponse()

        try:
            vectors = self.model_manager.embed_batch(list(request.texts))
            responses = [
                embedding_pb2.EmbeddingResponse(dimension=len(v), embedding=v) 
                for v in vectors
            ]
            return embedding_pb2.EmbeddingBatchResponse(embeddings=responses)
        except RuntimeError as e:
            context.set_code(grpc.StatusCode.UNAVAILABLE)
            context.set_details(str(e))
            return embedding_pb2.EmbeddingBatchResponse()
        except Exception as e:
            logger.exception("Error generating embeddings batch")
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(f"Internal error: {str(e)}")
            return embedding_pb2.EmbeddingBatchResponse()

def serve():
    logging.basicConfig(level=logging.INFO)
    logger.info("Initializing ModelManager (loading model in background)...")
    model_manager = ModelManager()
    
    # Preload the model
    # Wait until it's loaded to ensure health check passes
    model_manager.load_model()
    
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    embedding_pb2_grpc.add_EmbeddingServiceServicer_to_server(
        EmbeddingServiceServicer(model_manager), server
    )

    # Add Health Checking
    health_servicer = health.HealthServicer(
        experimental_non_blocking=True,
        experimental_thread_pool=futures.ThreadPoolExecutor(max_workers=1)
    )
    health_pb2_grpc.add_HealthServicer_to_server(health_servicer, server)
    health_servicer.set("embedding.EmbeddingService", health_pb2.HealthCheckResponse.SERVING)

    server.add_insecure_port('[::]:50051')
    server.start()
    logger.info("gRPC Server started on port 50051")
    server.wait_for_termination()

if __name__ == '__main__':
    serve()
