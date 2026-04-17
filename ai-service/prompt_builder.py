
from typing import List, Dict


def build_context(query: str, ranked_items: List[Dict]) -> str:
    """
    Organize the query and search results into structured context text.
    ranked_items example:
    [
        {
            "word": "dog",
            "graphScore": 1.0,
            "trendScore": 0.0033,
            "embeddingScore": 1.0,
            "finalScore": 0.80
        },
        ...
    ]
    """
    lines = [f'User query: "{query}"', "", "Retrieved semantic candidates:"]

    if not ranked_items:
        lines.append("No candidates were retrieved.")
        return "\n".join(lines)

    for idx, item in enumerate(ranked_items, start=1):
        word = item.get("word", "")
        graph_score = float(item.get("graphScore", 0.0))
        trend_score = float(item.get("trendScore", 0.0))
        embedding_score = float(item.get("embeddingScore", 0.0))
        final_score = float(item.get("finalScore", 0.0))

        lines.append(
            f'{idx}. {word} '
            f'(graph={graph_score:.4f}, '
            f'trend={trend_score:.6f}, '
            f'embedding={embedding_score:.4f}, '
            f'final={final_score:.4f})'
        )

    return "\n".join(lines)


def build_prompt(query: str, ranked_items: List[Dict]) -> str:
    
    context = build_context(query, ranked_items)

    instruction = (
        "\n\n"
        "Instruction:\n"
        "Based on the retrieved semantic candidates, write a short explanation of:\n"
        "1. what the query most likely refers to,\n"
        "2. why the top-ranked words are relevant,\n"
        "3. which signals (graph, trend, embedding) contributed to the result.\n"
        "Keep the explanation concise and grounded in the retrieved candidates."
    )

    return context + instruction