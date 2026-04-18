package ngordnet.ngrams;

import java.util.HashMap;

import ngordnet.algs4.In;
import ngordnet.algs4.Digraph;
import java.util.Collection;
import java.util.Map;
import java.util.List;

/**
 * An object that provides utility methods for making queries on the
 * Google NGrams dataset (or a subset thereof).
 *
 * An NGramMap stores pertinent data from a "words file" and a "counts
 * file". It is not a map in the strict sense, but it does provide additional
 * functionality.
 */
public class NGramMap {

    private static final int MIN_YEAR = 1400;
    private static final int MAX_YEAR = 2100;

    private Map<String, TimeSeries> wordCount;  // maps word to TimeSeries of word count per year
    private TimeSeries totalCount;  // TimeSeries of total word count per year



    /**
     * Constructs an NGramMap from WORDSFILENAME and COUNTSFILENAME.
     */
    public NGramMap(String wordsFilename, String countsFilename) {
        this.wordCount = new HashMap<>();
        this.totalCount = new TimeSeries();

        // Read in words file
        In wordsFile = new In(wordsFilename);
        while (wordsFile.hasNextLine()) {
            String[] line = wordsFile.readLine().split("\t");
            String word = line[0];
            int year = Integer.parseInt(line[1]);
            double count = Double.parseDouble(line[2]);
            if (year < MIN_YEAR || year > MAX_YEAR) {
                continue;
            }
            if (!wordCount.containsKey(word)) {
                wordCount.put(word, new TimeSeries());
            }
            wordCount.get(word).put(year, count);
        }

        // Read in counts file
        In countsFile = new In(countsFilename);
        while (countsFile.hasNextLine()) {
            String[] line = countsFile.readLine().split(",");
            int year = Integer.parseInt(line[0]);
            double tCount = Double.parseDouble(line[1]);
            if (year < MIN_YEAR || year > MAX_YEAR) {
                continue;
            }
            this.totalCount.put(year, tCount);
        }
    }

    /**
     * Provides the history of WORD between STARTYEAR and ENDYEAR, inclusive of both ends. The
     * returned TimeSeries should be a copy, not a link to this NGramMap's TimeSeries. In other
     * words, changes made to the object returned by this function should not also affect the
     * NGramMap. This is also known as a "defensive copy".
     */
    public TimeSeries countHistory(String word, int startYear, int endYear) {
        if (!this.wordCount.containsKey(word)) {
            return new TimeSeries();
        }

        TimeSeries countHistory = new TimeSeries();
        for (int year = startYear; year <= endYear; year++) {
            if (wordCount.get(word).contains(year)) {
                countHistory.put(year, wordCount.get(word).get(year));
            }
        }
        return countHistory;
    }

    /**
     * Provides the history of WORD. The returned TimeSeries should be a copy,
     * not a link to this NGramMap's TimeSeries. In other words, changes made
     * to the object returned by this function should not also affect the
     * NGramMap. This is also known as a "defensive copy".
     */
    public TimeSeries countHistory(String word) {
        if (!this.wordCount.containsKey(word)) {
            return new TimeSeries();
        }
        return countHistory(word, MIN_YEAR, MAX_YEAR);
    }

    /**
     * Returns a defensive copy of the total number of words recorded per year in all volumes.
     */
    public TimeSeries totalCountHistory() {
        TimeSeries copyTotalCountWord = new TimeSeries();
        for (int year : this.totalCount.years()) {
            copyTotalCountWord.put(year, this.totalCount.get(year));
        }
        return copyTotalCountWord;
    }



    /**
     * Provides a TimeSeries containing the relative frequency per year of WORD between STARTYEAR
     * and ENDYEAR, inclusive of both ends.
     */
    public TimeSeries weightHistory(String word, int startYear, int endYear) {
        /**getting the total count history for all words
         *returns a TimeSeries containing the total count of all words per year.*/
        TimeSeries totalCountWordHistory = totalCountHistory();
        if (!this.wordCount.containsKey(word)) { //each word to its count history
            return new TimeSeries();
        }
        /**If the given word is found, the count history for the word is obtained using the countHistory method,
         * which returns a TimeSeries containing the count of the given word per year.*/
        TimeSeries countHistory = countHistory(word).subset(startYear, endYear);
        /**The dividedBy method is then called on the count history of the word and
         *the subset of the total count history, to obtain the relative frequency per year of the word.*/
        return countHistory.dividedBy(totalCountWordHistory.subset(startYear, endYear));
    }



    /**
     * Provides a TimeSeries containing the relative frequency per year of WORD compared to
     * all words recorded in that year. If the word is not in the data files, return an empty
     * TimeSeries.
     */
    public TimeSeries weightHistory(String word) {
        TimeSeries totalCountHistory = totalCountHistory();
        if (!this.wordCount.containsKey(word)) {
            return new TimeSeries();
        }
        TimeSeries countHistory = countHistory(word);
        List<Integer> years = countHistory.years();
        int startYear = years.get(0);
        int endYear = years.get(years.size() - 1);
        return countHistory.dividedBy(totalCountHistory.subset(startYear, endYear));
    }

    /**
     * Provides the summed relative frequency per year of all words in WORDS
     * between STARTYEAR and ENDYEAR, inclusive of both ends. If a word does not exist in
     * this time frame, ignore it rather than throwing an exception.
     */
    public TimeSeries summedWeightHistory(Collection<String> words, int startYear, int endYear) {
        TimeSeries summedFW = new TimeSeries();

        for (String word : words) {
            TimeSeries weightHistory = weightHistory(word, startYear, endYear);
            summedFW = summedFW.plus(weightHistory);
        }

        return summedFW;

    }

    /**
     * Returns the summed relative frequency per year of all words in WORDS.
     */
    public TimeSeries summedWeightHistory(Collection<String> words) {
        TimeSeries summedFW = new TimeSeries();

        for (String word : words) {
            TimeSeries weightHistory = weightHistory(word);
            summedFW = summedFW.plus(weightHistory);
        }

        return summedFW;

    }

    public int countInYearRange(String word, int startYear, int endYear) {
        int totalCount = 0;
        for (int year = startYear; year <= endYear; year++) {
            if (this.wordCount.containsKey(word) && this.wordCount.get(word).contains(year)) {
                totalCount += this.wordCount.get(word).get(year);
            }
        }
        return totalCount;
    }

}



