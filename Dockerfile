
FROM maven:3.9-eclipse-temurin-17

RUN apt-get update && apt-get install -y python3 python3-venv python3-pip curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml .
COPY ngordnet ./ngordnet
COPY src ./src
COPY data ./data
COPY ai-service ./ai-service

RUN python3 -m venv /opt/venv && \
    /opt/venv/bin/pip install --upgrade pip setuptools wheel && \
    /opt/venv/bin/pip install --no-cache-dir \
        torch --index-url https://download.pytorch.org/whl/cpu && \
    /opt/venv/bin/pip install --no-cache-dir -r /app/ai-service/requirements.txt

RUN mvn clean compile -DskipTests

ENV PORT=10000
EXPOSE 10000

CMD ["bash", "-lc", "\
mkdir -p /app/data/ngrams && \
if [ ! -f /app/data/ngrams/top_14377_words.csv ]; then curl -L \"$NGRAM_WORDS_URL\" -o /app/data/ngrams/top_14377_words.csv; fi && \
if [ ! -f /app/data/ngrams/total_counts.csv ]; then curl -L \"$NGRAM_COUNTS_URL\" -o /app/data/ngrams/total_counts.csv; fi && \
mvn -q exec:java \
"]