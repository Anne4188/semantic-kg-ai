
import json
import math
import re
import sys
from pathlib import Path
from typing import Dict, List, Tuple
from urllib.parse import quote
from urllib.request import urlopen

# Enable Python to import modules in ai-service
BASE_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = BASE_DIR.parent
AI_SERVICE_DIR = PROJECT_ROOT / "ai-service"
sys.path.append(str(AI_SERVICE_DIR))

from semantic_rerank import rerank  


QUERIES_PATH = BASE_DIR / "queries.json"
LABELS_PATH = BASE_DIR / "labeled_relevance.json"

API_BASE = "http://127.0.0.1:4567"
START_YEAR = 1900
END_YEAR = 2020

GRAPH_TOP_K = 40      # The number of retrieval candidates in the first stage
FINAL_EVAL_K = 5      # Final assessment top-k


def load_queries() -> List[str]:
    with open(QUERIES_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


def load_labels() -> Dict[str, Dict[str, List[str]]]:
    with open(LABELS_PATH, "r", encoding="utf-8") as f:
        return json.load(f)


def parse_hyponyms_response_with_scores(raw_text: str) -> List[dict]:
    """
    Parse the return result of Java /hyponyms.
    Supported formats：
    [dog [graph=1.0, trend=0.0033, final=0.7010], click [graph=1.0, trend=0.0008, final=0.7002]]
    """
    text = raw_text.strip()

    if text.startswith('"') and text.endswith('"'):
        text = text[1:-1]

    text = text.replace("\\u003d", "=")

    if text.startswith("[") and text.endswith("]"):
        text = text[1:-1]

    if not text.strip():
        return []

    items = re.split(r',\s*(?![^\[]*\])', text)

    parsed = []
    pattern = re.compile(
        r'^(.*?)\s+\[graph=([0-9Ee\.\-]+),\s*trend=([0-9Ee\.\-]+),\s*final=([0-9Ee\.\-]+)\]$'
    )

    for item in items:
        item = item.strip()
        match = pattern.match(item)
        if match:
            word = match.group(1).strip()
            graph_score = float(match.group(2))
            trend_score = float(match.group(3))
            final_score = float(match.group(4))
            parsed.append({
                "word": word,
                "graphScore": graph_score,
                "trendScore": trend_score,
                "finalScore": final_score,
            })
        else:
            if item:
                parsed.append({
                    "word": item,
                    "graphScore": 0.0,
                    "trendScore": 0.0,
                    "finalScore": 0.0,
                })

    return parsed


def call_hyponyms_api_with_scores(query: str) -> List[dict]:
    url = (
        f"{API_BASE}/hyponyms?"
        f"words={quote(query)}&startYear={START_YEAR}&endYear={END_YEAR}&k={GRAPH_TOP_K}"
    )
    try:
        with urlopen(url, timeout=20) as response:
            raw = response.read().decode("utf-8")
        return parse_hyponyms_response_with_scores(raw)
    except Exception as e:
        print(f"[WARN] API call failed for query='{query}': {e}")
        return []


def get_graph_trend_results(query: str) -> List[str]:
    scored_items = call_hyponyms_api_with_scores(query)
    return [item["word"] for item in scored_items]


def get_hybrid_rerank_results(query: str) -> List[str]:
    scored_items = call_hyponyms_api_with_scores(query)
    reranked_items = rerank(query, scored_items)
    return [item["word"] for item in reranked_items]


def build_relevance_map(label_entry: Dict[str, List[str]]) -> Dict[str, int]:
    relevance = {}
    for word in label_entry.get("highly_relevant", []):
        relevance[word] = 2
    for word in label_entry.get("relevant", []):
        if word not in relevance:
            relevance[word] = 1
    return relevance


def precision_at_k(results: List[str], relevant_set: set, k: int) -> float:
    top_k = results[:k]
    if not top_k:
        return 0.0
    hits = sum(1 for r in top_k if r in relevant_set)
    return hits / len(top_k)


def recall_at_k(results: List[str], relevant_set: set, k: int) -> float:
    if not relevant_set:
        return 0.0
    top_k = results[:k]
    hits = sum(1 for r in top_k if r in relevant_set)
    return hits / len(relevant_set)


def dcg_at_k(results: List[str], relevance_map: Dict[str, int], k: int) -> float:
    dcg = 0.0
    for i, word in enumerate(results[:k], start=1):
        rel = relevance_map.get(word, 0)
        if rel > 0:
            dcg += rel / math.log2(i + 1)
    return dcg


def ndcg_at_k(results: List[str], relevance_map: Dict[str, int], k: int) -> float:
    actual_dcg = dcg_at_k(results, relevance_map, k)
    ideal_results = sorted(
        relevance_map.keys(),
        key=lambda w: relevance_map[w],
        reverse=True
    )
    ideal_dcg = dcg_at_k(ideal_results, relevance_map, k)
    if ideal_dcg == 0:
        return 0.0
    return actual_dcg / ideal_dcg


def evaluate_model(
    model_name: str,
    queries: List[str],
    labels: Dict[str, Dict[str, List[str]]],
    k: int = 5
) -> Tuple[float, float, float]:
    precisions = []
    recalls = []
    ndcgs = []

    for query in queries:
        if query not in labels:
            continue

        if model_name == "graph_trend":
            results = get_graph_trend_results(query)
        elif model_name == "hybrid_rerank":
            results = get_hybrid_rerank_results(query)
        else:
            raise ValueError(f"Unknown model: {model_name}")

        label_entry = labels[query]
        relevant_set = set(label_entry.get("highly_relevant", [])) | set(label_entry.get("relevant", []))
        relevance_map = build_relevance_map(label_entry)

        p = precision_at_k(results, relevant_set, k)
        r = recall_at_k(results, relevant_set, k)
        n = ndcg_at_k(results, relevance_map, k)

        precisions.append(p)
        recalls.append(r)
        ndcgs.append(n)

        print(f"Query: {query}")
        print(f"  Results: {results[:k]}")
        print(f"  Relevant: {sorted(relevant_set)}")
        print(f"  Precision@{k}: {p:.4f}")
        print(f"  Recall@{k}:    {r:.4f}")
        print(f"  NDCG@{k}:      {n:.4f}")
        print()

    avg_precision = sum(precisions) / len(precisions) if precisions else 0.0
    avg_recall = sum(recalls) / len(recalls) if recalls else 0.0
    avg_ndcg = sum(ndcgs) / len(ndcgs) if ndcgs else 0.0

    return avg_precision, avg_recall, avg_ndcg


def main():
    queries = load_queries()
    labels = load_labels()
    k = FINAL_EVAL_K

    models = ["graph_trend", "hybrid_rerank"]

    print(f"Evaluating models at K={k}")
    print("-" * 60)

    for model in models:
        precision, recall, ndcg = evaluate_model(model, queries, labels, k=k)
        print(f"Model: {model}")
        print(f"  Precision@{k}: {precision:.4f}")
        print(f"  Recall@{k}:    {recall:.4f}")
        print(f"  NDCG@{k}:      {ndcg:.4f}")
        print("-" * 60)


if __name__ == "__main__":
    main()