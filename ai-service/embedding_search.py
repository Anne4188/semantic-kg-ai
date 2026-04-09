from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity


class EmbeddingSearch:
    def __init__(self, model_name: str = "all-MiniLM-L6-v2"):
        self.model = SentenceTransformer(model_name)

    # using for semantic reranking
    def score_candidates(self, query: str, candidates: list[str]) -> dict[str, float]:
        query_embedding = self.model.encode([query])
        candidate_embeddings = self.model.encode(candidates)

        scores = cosine_similarity(query_embedding, candidate_embeddings)[0]
        return {word: float(score) for word, score in zip(candidates, scores)}

    # keep the original sorting method
    def rank(self, query: str, candidates: list[str]) -> list[tuple[str, float]]:
        query_embedding = self.model.encode([query])
        candidate_embeddings = self.model.encode(candidates)

        scores = cosine_similarity(query_embedding, candidate_embeddings)[0]
        ranked = sorted(
            zip(candidates, scores),
            key=lambda x: x[1],
            reverse=True
        )
        return [(word, float(score)) for word, score in ranked]


if __name__ == "__main__":
    query = "dog"
    candidates = [
        "puppy",
        "canine",
        "pet",
        "cat",
        "wolf",
        "car",
        "toy",
        "animal"
    ]

    search = EmbeddingSearch()
    results = search.rank(query, candidates)

    print(f"Query: {query}")
    print("Embedding similarity ranking:")
    for word, score in results:
        print(f"{word}: {score:.4f}")