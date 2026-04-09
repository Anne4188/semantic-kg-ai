from embedding_search import EmbeddingSearch


def rerank(query: str, candidates_with_scores: list[dict]) -> list[dict]:
    candidates = [item["word"] for item in candidates_with_scores]

    embedder = EmbeddingSearch()
    embedding_scores = embedder.score_candidates(query, candidates)

    reranked = []
    for item in candidates_with_scores:
        word = item["word"]
        graph_score = item["graphScore"]
        trend_score = item["trendScore"]
        embedding_score = embedding_scores[word]

        final_score = (
            0.4 * graph_score
            + 0.2 * trend_score
            + 0.4 * embedding_score
        )

        reranked.append({
            "word": word,
            "graphScore": graph_score,
            "trendScore": trend_score,
            "embeddingScore": embedding_score,
            "finalScore": final_score
        })

    reranked.sort(key=lambda x: x["finalScore"], reverse=True)
    return reranked


if __name__ == "__main__":
    query = "dog"

    candidates_with_scores = [
        {
            "word": "dog",
            "graphScore": 1.0,
            "trendScore": 0.0033662829990008013
        },
        {
            "word": "click",
            "graphScore": 1.0,
            "trendScore": 8.520231740551808E-4
        },
        {
            "word": "frank",
            "graphScore": 1.0,
            "trendScore": 5.309348337662761E-4
        },
        {
            "word": "heel",
            "graphScore": 1.0,
            "trendScore": 4.831110756791522E-4
        },
        {
            "word": "toy",
            "graphScore": 0.5,
            "trendScore": 4.619500923266326E-4
        },
    ]
    
    results = rerank(query, candidates_with_scores)

    print(f"Query: {query}")
    print("Semantic reranking results:")
    for item in results:
        print(
            f'{item["word"]}: '
            f'graph={item["graphScore"]:.4f}, '
            f'trend={item["trendScore"]:.6f}, '
            f'embedding={item["embeddingScore"]:.4f}, '
            f'final={item["finalScore"]:.4f}'
        )