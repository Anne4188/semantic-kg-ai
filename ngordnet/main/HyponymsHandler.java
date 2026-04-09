package ngordnet.main;

import java.io.IOException;
import java.util.*;

import ngordnet.browser.NgordnetQuery;
import ngordnet.browser.NgordnetQueryHandler;
import ngordnet.ngrams.NGramMap;
import spark.Request;
import spark.Response;

public class HyponymsHandler extends NgordnetQueryHandler {
    private WordNet insWordNet;
    private NGramMap insNGramMap;
    private TrendScorer trendScorer;
    private WeightedRanker weightedRanker;
    private static final int DEFAULT_K = 10;

    public HyponymsHandler(NGramMap ngm, WordNet wNet) throws IOException {
        insWordNet = wNet;
        insNGramMap = ngm;
        trendScorer = new TrendScorer(ngm);
        weightedRanker = new WeightedRanker();
    }

    private List<String> getTopHyponyms(String sourceWord, List<String> hyponyms, int startYear, int endYear, int k) {
        if (hyponyms.isEmpty()) {
            return Collections.emptyList();
        }

        List<ScoredWord> scoredWords = new ArrayList<>();

        for (String hyponym : hyponyms) {
            int distance = insWordNet.distance(sourceWord, hyponym);
            double graphScore;
            if (distance == Integer.MAX_VALUE) {
                graphScore = 0.0;
            } else {
                graphScore = 1.0 / (distance + 1);
            }

            double trendScore = trendScorer.score(hyponym, startYear, endYear);
            double finalScore = weightedRanker.score(graphScore, trendScore);

            System.out.println("==== HYBRID SCORE DEBUG ====");
            System.out.printf(
                    "hyponym=%s, graphScore=%.4f, trendScore=%.8f, finalScore=%.8f%n",
                    hyponym, graphScore, trendScore, finalScore
            );

            if (trendScore > 0) {
                scoredWords.add(new ScoredWord(hyponym, graphScore, trendScore, finalScore));
            }
        }

        scoredWords.sort((a, b) -> {
            int cmp = Double.compare(b.score, a.score);
            if (cmp != 0) {
                return cmp;
            }
            return a.word.compareTo(b.word);
        });

        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(k, scoredWords.size()); i++) {
            ScoredWord sw = scoredWords.get(i);
            result.add(
                    sw.word
                            + " [graph=" + sw.graphScore
                            + ", trend=" + sw.trendScore
                            + ", final=" + sw.score + "]"
            );
        }

        return result;
    }

    @Override
    public String handle(NgordnetQuery q) {
        List<String> words = q.words();
        int startYear = q.startYear();
        int endYear = q.endYear();
        int k = q.k();

        if (k == -1) {
            k = DEFAULT_K;
        }

        List<Set<String>> hyponymsList = new ArrayList<>();
        for (String word : words) {
            Set<String> hyponyms = insWordNet.hyponyms(word, startYear, endYear, k);
            hyponymsList.add(hyponyms);
        }

        Set<String> commonHyponyms = new HashSet<>();
        if (!hyponymsList.isEmpty()) {
            commonHyponyms = new HashSet<>(hyponymsList.get(0));
            for (int i = 1; i < hyponymsList.size(); i++) {
                commonHyponyms.retainAll(hyponymsList.get(i));
            }
        }

        if (commonHyponyms.isEmpty()) {
            return new ArrayList<String>().toString();
        }

        if (k == 0) {
            List<String> sortedList = new ArrayList<>(commonHyponyms);
            Collections.sort(sortedList);
            return sortedList.toString();
        }

        String sourceWord = words.get(0);
        List<String> topHyponyms = getTopHyponyms(sourceWord, new ArrayList<>(commonHyponyms), startYear, endYear, k);
        
        return topHyponyms.toString();
    }

    @Override
    public Object processQuery(Request request, Response response, NGramMap ngm) {
        String word = request.queryParams("word");
        int startYear = Integer.parseInt(request.queryParams("start"));
        int endYear = Integer.parseInt(request.queryParams("end"));
        int count = Integer.parseInt(request.queryParams("count"));

        NgordnetQuery query = new NgordnetQuery(Arrays.asList(word), startYear, endYear, count);
        return handle(query);
    }

    @Override
    public Object processQuery(NgordnetQuery query, NGramMap ngm) {
        return handle(query);
    }

    private static class ScoredWord {
        String word;
        double graphScore;
        double trendScore;
        double score;

        ScoredWord(String word, double graphScore, double trendScore, double score) {
            this.word = word;
            this.graphScore = graphScore;
            this.trendScore = trendScore;
            this.score = score;
        }
    }
}