
//HistoryHandler.java

package ngordnet.main;

import ngordnet.browser.NgordnetQuery;
import ngordnet.browser.NgordnetQueryHandler;
import ngordnet.ngrams.NGramMap;
import ngordnet.ngrams.TimeSeries;
import ngordnet.plotting.Plotter;
import org.knowm.xchart.XYChart;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryHandler extends NgordnetQueryHandler {
    private NGramMap ngMap;

    public HistoryHandler(NGramMap map) {
        this.ngMap = map;
    }

    @Override
    public String handle(NgordnetQuery q) {
        return (String) processQuery(q, ngMap);
    }

    @Override
    public Object processQuery(NgordnetQuery q, NGramMap ngm) {
        List<String> words = q.words();
        int startYear = q.startYear();
        int endYear = q.endYear();

        List<TimeSeries> lts = new ArrayList<>();
        for (String word : words) {
            lts.add(ngm.weightHistory(word, startYear, endYear));
        }

        XYChart chart = Plotter.generateTimeSeriesChart(words, lts);
        return Plotter.encodeChartAsString(chart);
    }

    @Override
    public Object processQuery(Request req, Response res, NGramMap ngm) {
        String[] rawWords = req.queryParamsValues("words");
        List<String> words;

        if (rawWords == null) {
            String oneWord = req.queryParams("word");
            if (oneWord == null || oneWord.isBlank()) {
                words = new ArrayList<>();
            } else {
                words = List.of(oneWord);
            }
        } else {
            words = Arrays.asList(rawWords);
        }

        int startYear = Integer.parseInt(req.queryParams("startYear"));
        int endYear = Integer.parseInt(req.queryParams("endYear"));

        List<TimeSeries> lts = new ArrayList<>();
        for (String word : words) {
            lts.add(ngm.weightHistory(word, startYear, endYear));
        }

        XYChart chart = Plotter.generateTimeSeriesChart(words, lts);
        return Plotter.encodeChartAsString(chart);
    }
}