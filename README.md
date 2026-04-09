# Semantic KG AI — Hybrid Semantic Ranking

## Overview

This project extends a WordNet-based semantic search system into a **hybrid semantic ranking engine** by combining:

* WordNet graph structure
* NGram temporal frequency trends
* Weighted hybrid scoring
* Web-based query interface

The system ranks semantic hyponyms using both **graph proximity** and **language usage trends**, similar to modern AI retrieval pipelines. Built a hybrid semantic ranking engine combining WordNet graph distance and Google NGram temporal trends, improving semantic retrieval relevance.Implemented weighted hybrid scoring (graph + trend) and top-k reranking pipeline using BFS-based semantic distance.Developed a web-based semantic search interface with REST API and real-time ranked hyponym retrieval.


---

## Features

### 1. WordNet Graph Retrieval

* Builds a directed semantic graph
* Supports hyponyms lookup
* BFS traversal for descendants

### 2. Graph Distance Scoring

Each candidate word is scored using:

```
graphScore = 1 / (distance + 1)
```

Closer semantic nodes receive higher scores.

---

### 3. Temporal Trend Scoring

Using Google NGram frequency:

```
trendScore = average frequency(startYear, endYear)
```

Words more commonly used in language receive higher scores.

---

### 4. Hybrid Ranking

Final ranking combines both signals:

```
finalScore = 0.7 * graphScore + 0.3 * trendScore
```

This creates a **hybrid semantic + statistical ranking system**.


## Hybrid Ranking Debug

![Hybrid Score](images/Hybrid-Score-Debug.png)



---

### 5. Web API

Example:

```
/hyponyms?words=dog&startYear=1900&endYear=2020&k=5
```

Returns ranked semantic candidates.


## API Result

![API Result](images/curl-API-result.png)


---

### 6. Interactive UI

Access:

```
http://127.0.0.1:4567/ngordnet.html
```

## UI Example

![UI](images/UI-result-1.png)

![UI](images/UI-result-2.png)

![UI](images/UI-result-3.png)


---

## Architecture

WordNet Graph
↓
Graph Distance Scoring
↓
NGram Trend Scoring
↓
Weighted Hybrid Ranker
↓
Top-K Semantic Results

---

## Example Output

```
dog [graph=1.0, trend=0.0033, final=0.7010]
click [graph=1.0, trend=0.0008, final=0.7002]
toy [graph=0.5, trend=0.0004, final=0.3501]
```

---

# The AI functions that have been completed now

Semantic graph retrieval
Graph distance scoring
Trend-based statistical scoring
Hybrid ranking
Top-k reranking
Explainable scoring
Web API
UI interface


## Tech Stack

* Java
* Maven
* Spark Java Web Server
* WordNet Dataset
* Google NGram Dataset

---

## Future Work

* Query expansion
* Embedding similarity
* Semantic reranking
* LLM integration

---
