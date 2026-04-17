

//HistoryTextHandler.java

package ngordnet.main;

import ngordnet.browser.NgordnetQuery;
import ngordnet.browser.NgordnetQueryHandler;
import ngordnet.ngrams.NGramMap;
import ngordnet.ngrams.TimeSeries;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryTextHandler extends NgordnetQueryHandler {
    private NGramMap ngMap;

    public HistoryTextHandler(NGramMap map) {
        this.ngMap = map;
    }

    @Override
    public String handle(NgordnetQuery q) {
        return (String) processQuery(q, ngMap);
    }

    @Override
    public Object processQuery(NgordnetQuery q, NGramMap ngm) {
        return buildHistoryText(q.words(), q.startYear(), q.endYear(), ngm);
    }

    @Override
    public Object processQuery(Request req, Response res, NGramMap ngm) {
        String[] rawWords = req.queryParamsValues("words");
        List<String> words;

        if (rawWords == null) {
            String oneWord = req.queryParams("word");
            if (oneWord == null || oneWord.isBlank()) {
                words = List.of();
            } else {
                words = List.of(oneWord);
            }
        } else {
            words = Arrays.asList(rawWords);
        }

        int startYear = Integer.parseInt(req.queryParams("startYear"));
        int endYear = Integer.parseInt(req.queryParams("endYear"));

        return buildHistoryText(words, startYear, endYear, ngm);
    }

    private String buildHistoryText(List<String> words, int startYear, int endYear, NGramMap map) {
        Map<String, TimeSeries> wordCounts = new HashMap<>();
        for (String word : words) {
            TimeSeries countsHistoryWord = map.weightHistory(word, startYear, endYear);
            TimeSeries intCounts = new TimeSeries();
            for (Integer year : countsHistoryWord.years()) {
                intCounts.put(year, countsHistoryWord.get(year));
            }
            wordCounts.put(word, intCounts);
        }

        StringBuilder response = new StringBuilder();
        for (String word : wordCounts.keySet()) {
            response.append(word)
                    .append(": ")
                    .append(wordCounts.get(word))
                    .append("\n");
        }
        return response.toString();
    }
}