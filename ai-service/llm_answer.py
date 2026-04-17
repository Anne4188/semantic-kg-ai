

import os
import io
import re
import json
import contextlib
import warnings
warnings.filterwarnings("ignore", category=SyntaxWarning)

from typing import List, Dict
from urllib.parse import quote
from urllib.request import urlopen

import requests

from prompt_builder import build_prompt

# Minimize Hugging Face/transformers output as much as possible
os.environ["HF_HUB_DISABLE_PROGRESS_BARS"] = "1"
os.environ["TOKENIZERS_PARALLELISM"] = "false"
os.environ["TRANSFORMERS_VERBOSITY"] = "error"

# model loading output when importing semantic_rerank
with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
    from semantic_rerank import rerank


# API_BASE = "http://127.0.0.1:4567"

API_BASE = os.environ.get("JAVA_API_BASE", "http://127.0.0.1:4567").strip()
START_YEAR = 1900
END_YEAR = 2020
GRAPH_TOP_K = 40
FINAL_TOP_K = 5

# ===== External LLM config =====
LLM_BASE_URL = os.environ.get("LLM_BASE_URL", "").strip()
LLM_API_KEY = os.environ.get("LLM_API_KEY", "").strip()
LLM_MODEL = os.environ.get("LLM_MODEL", "gpt-4.1-mini").strip()



def parse_hyponyms_response_with_scores(raw_text: str) -> List[Dict]:
    text = raw_text.strip()

    if text.startswith('"') and text.endswith('"'):
        text = text[1:-1]

    text = text.replace("\\u003d", "=")

    if text.startswith("[") and text.endswith("]"):
        text = text[1:-1]

    if not text:
        return []

    # Split by "]" instead of using complex regex
    raw_items = text.split("],")

    parsed = []
    pattern = re.compile(
        r'(.*?)\s*\[graph=([0-9Ee\.\-]+),\s*trend=([0-9Ee\.\-]+),\s*final=([0-9Ee\.\-]+)\]'
    )

    for item in raw_items:
        item = item.strip()

        if not item.endswith("]"):
            item += "]"

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

    return parsed


def fetch_candidates_from_java(query: str) -> List[Dict]:
    """
    Obtain the real candidates and scores from the Java /hyponyms API.
    """
    url = (
        f"{API_BASE}/hyponyms?"
        f"words={quote(query)}&startYear={START_YEAR}&endYear={END_YEAR}&k={GRAPH_TOP_K}"
    )

    with urlopen(url, timeout=60) as response:
        raw = response.read().decode("utf-8")

    return parse_hyponyms_response_with_scores(raw)


def get_real_ranked_items(query: str) -> List[Dict]:

    """
    Java retrieval -> Python semantic rerank -> final top-k
    """
    candidates_with_scores = fetch_candidates_from_java(query)
    if not candidates_with_scores:
        return []

    reranked_items = rerank(query, candidates_with_scores)
    
    return reranked_items[:FINAL_TOP_K]


def generate_template_answer(query: str, ranked_items: List[Dict]) -> str:
    """
    Default template response（rule-based）。
    """
    if not ranked_items:
        return (
            f'For the query "{query}", the system did not retrieve any strong semantic candidates. '
            f'This may indicate limited graph coverage or insufficient candidate recall.'
        )

    top_items = ranked_items[:5]
    top_words = [item["word"] for item in top_items]

    best_word = top_words[0]
    other_words = top_words[1:]

    best_item = top_items[0]
    graph_score = float(best_item.get("graphScore", 0.0))
    trend_score = float(best_item.get("trendScore", 0.0))
    embedding_score = float(best_item.get("embeddingScore", 0.0))
    final_score = float(best_item.get("finalScore", 0.0))

    if other_words:
        related_phrase = ", ".join(other_words)
        related_sentence = (
            f'Other high-ranked candidates such as {related_phrase} '
            f'help explain the semantic neighborhood of the query. '
        )
    else:
        related_sentence = ""

    answer = (
        f'For the query "{query}", the system identifies "{best_word}" '
        f'as the strongest semantic result. '
        f'{related_sentence}'
        f'The top result receives a graph score of {graph_score:.4f}, '
        f'a trend score of {trend_score:.6f}, '
        f'an embedding score of {embedding_score:.4f}, '
        f'and a final hybrid score of {final_score:.4f}. '
        f'This indicates that the result is supported by knowledge graph proximity, '
        f'historical language usage, and neural semantic similarity.'
    )

    return answer


def call_external_llm(prompt: str) -> str:
    if not LLM_BASE_URL:
        raise RuntimeError("LLM_BASE_URL is not set.")
    if not LLM_API_KEY:
        raise RuntimeError("LLM_API_KEY is not set.")

    url = f"{LLM_BASE_URL.rstrip('/')}/chat/completions"

    headers = {
        "Authorization": f"Bearer {LLM_API_KEY}",
        "Content-Type": "application/json; charset=utf-8",
    }

    safe_prompt = prompt.encode("ascii", errors="ignore").decode("ascii")

    payload = {
        "model": LLM_MODEL,
        "messages": [
            {
                "role": "system",
                "content": (
                    "You are a semantic search explanation assistant. "
                    "Use only the retrieved candidates and scores provided in the prompt. "
                    "Do not invent extra candidates."
                ),
            },
            {
                "role": "user",
                "content": safe_prompt,
            },
        ],
        "temperature": 0.2,
    }

    response = requests.post(url, headers=headers, json=payload, timeout=60)
    response.raise_for_status()

    data = response.json()
    return data["choices"][0]["message"]["content"].strip()



def generate_answer_with_prompt(query: str, use_llm: bool = False) -> Dict[str, object]:
    """
    Main entrance to the outside Input： "query"
    Output： prompt + answer + reranked items
    """
    ranked_items = get_real_ranked_items(query)
    prompt = build_prompt(query, ranked_items)

    if use_llm:
        try:
            answer = call_external_llm(prompt)
        except Exception as e:
            # When the LLM fails, roll back to template answer to avoid the entire interface crashing
            answer = (
                f"[LLM fallback triggered: {type(e).__name__}: {e}] "
                + generate_template_answer(query, ranked_items)
            )
    else:
        answer = generate_template_answer(query, ranked_items)

    return {
        "prompt": prompt,
        "answer": answer,
        "ranked_items": ranked_items,
    }


if __name__ == "__main__":
    import sys

    query = "dog"
    use_llm = False

    for arg in sys.argv[1:]:
        if arg == "--use-llm":
            use_llm = True
        else:
            query = arg

    result = generate_answer_with_prompt(query, use_llm=use_llm)

    # When calling Java, only print the final answer
    print(result["answer"])






