package ngordnet.main;

public class WeightedRanker {
    public double score(double graphScore, double trendScore) {
        return 0.7 * graphScore + 0.3 * trendScore;
    }
}