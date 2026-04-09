package ngordnet.main;

import ngordnet.ngrams.NGramMap;
import ngordnet.ngrams.TimeSeries;

public class TrendScorer {
    private NGramMap ngm;

    public TrendScorer(NGramMap ngm) {
        this.ngm = ngm;
    }

    public double score(String word, int startYear, int endYear) {
        TimeSeries ts = ngm.weightHistory(word, startYear, endYear);
        double sum = 0.0;
        for (Double v : ts.data()) {
            sum += v;
        }
        return sum;
    }
}