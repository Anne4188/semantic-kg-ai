package ngordnet.proj2b_testing;

import ngordnet.browser.NgordnetQuery;
import ngordnet.browser.NgordnetQueryHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/** Tests the case where the list of words is length greater than 1, but k is still zero. */
public class TestMultiWordK0Hyponyms {
    // this case doesn't use the NGrams dataset at all, so the choice of files is irrelevant
    public static final String WORDS_FILE = "data/ngrams/very_short.csv";
    public static final String TOTAL_COUNTS_FILE = "data/ngrams/total_counts.csv";
    public static final String SMALL_SYNSET_FILE = "data/wordnet/synsets16.txt";
    public static final String SMALL_HYPONYM_FILE = "data/wordnet/hyponyms16.txt";
    public static final String LARGE_SYNSET_FILE = "data/wordnet/synsets.txt";
    public static final String LARGE_HYPONYM_FILE = "data/wordnet/hyponyms.txt";
    public static final String ASORDS_FILE ="data/ngrams/top_14377_words.csv";

    /** This is an example from the spec.*/
    @Test
    public void testOccurrenceAndChangeK0() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymHandler(
                WORDS_FILE, TOTAL_COUNTS_FILE, SMALL_SYNSET_FILE, SMALL_HYPONYM_FILE);
        List<String> words = List.of("occurrence", "change");

        NgordnetQuery nq = new NgordnetQuery(words, 0, 0, 0);
        String actual = studentHandler.handle(nq);
        String expected = "[alteration, change, increase, jump, leap, modification, saltation, transition]";
        assertThat(actual).isEqualTo(expected);
    }

    //  Add more unit tests (including edge case tests) here.
    //Create similar unit test files for the k != 0 cases.
    @Test
    public void testEmptyWordsList() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymHandler(
                WORDS_FILE, TOTAL_COUNTS_FILE, SMALL_SYNSET_FILE, SMALL_HYPONYM_FILE);
        List<String> words = List.of();

        NgordnetQuery nq = new NgordnetQuery(words, 0, 0, 0);
        String actual = studentHandler.handle(nq);
        String expected = "[]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testSingleWordNoHyponyms() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymHandler(
                WORDS_FILE, TOTAL_COUNTS_FILE, LARGE_SYNSET_FILE, SMALL_HYPONYM_FILE);
        List<String> words = List.of("laborious");

        NgordnetQuery nq = new NgordnetQuery(words, 0, 0, 0);
        String actual = studentHandler.handle(nq);
        String expected = "[]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testMultipleWordsNoCommonHyponyms() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymHandler(
                WORDS_FILE, TOTAL_COUNTS_FILE, SMALL_SYNSET_FILE, SMALL_HYPONYM_FILE);
        List<String> words = List.of("blueberry", "apple");

        NgordnetQuery nq = new NgordnetQuery(words, 0, 0, 0);
        String actual = studentHandler.handle(nq);
        String expected = "[]";
        assertThat(actual).isEqualTo(expected);
    }



    @Test
    public void testWordsNotInSynsetsFile() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymHandler(
                WORDS_FILE, TOTAL_COUNTS_FILE, SMALL_SYNSET_FILE, SMALL_HYPONYM_FILE);
        List<String> words = List.of("pineapple", "durian");

        NgordnetQuery nq = new NgordnetQuery(words, 0, 0, 0);
        String actual = studentHandler.handle(nq);
        String expected = "[]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWordsNotInSile43() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymHandler(
                ASORDS_FILE, TOTAL_COUNTS_FILE, LARGE_SYNSET_FILE, LARGE_HYPONYM_FILE);
        List<String> words = List.of("entity");

        NgordnetQuery nq = new NgordnetQuery(words, 1470, 2019, 6);
        String actual = studentHandler.handle(nq);
        String expected = "[are, at, have, he, in, one]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void teststentWordK42() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymHandler(
                ASORDS_FILE, TOTAL_COUNTS_FILE, LARGE_SYNSET_FILE, LARGE_HYPONYM_FILE);
        List<String> words = List.of("entity");

        NgordnetQuery nq = new NgordnetQuery(words,  1470, 2019, 3);
        String actual = studentHandler.handle(nq);

        String expected = "[are, at, in]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void teststeWordK44() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymHandler(
                ASORDS_FILE, TOTAL_COUNTS_FILE, LARGE_SYNSET_FILE, LARGE_HYPONYM_FILE);
        List<String> words = List.of("quality", "callousness");

        NgordnetQuery nq = new NgordnetQuery(words,  1470, 2019, 4);
        String actual = studentHandler.handle(nq);

        String expected = "[hardness]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void teststWrdK45() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymHandler(
                ASORDS_FILE, TOTAL_COUNTS_FILE, LARGE_SYNSET_FILE, LARGE_HYPONYM_FILE);
        List<String> words = List.of("soul", "dork");

        NgordnetQuery nq = new NgordnetQuery(words,  1470, 2019, 8);
        String actual = studentHandler.handle(nq);

        String expected = "[jerk]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWrdK46() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymHandler(
                ASORDS_FILE, TOTAL_COUNTS_FILE, LARGE_SYNSET_FILE, LARGE_HYPONYM_FILE);
        List<String> words = List.of("musician", "orchestrator");

        NgordnetQuery nq = new NgordnetQuery(words,  1920, 1980, 9);
        String actual = studentHandler.handle(nq);

        String expected = "[]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWrdK43() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymHandler(
                ASORDS_FILE, TOTAL_COUNTS_FILE, LARGE_SYNSET_FILE, LARGE_HYPONYM_FILE);
        List<String> words = List.of("genus");

        NgordnetQuery nq = new NgordnetQuery(words,  1470, 2019, 6);
        String actual = studentHandler.handle(nq);

        String expected = "[genus]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWrdK430() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymHandler(
                ASORDS_FILE, TOTAL_COUNTS_FILE, LARGE_SYNSET_FILE, LARGE_HYPONYM_FILE);
        List<String> words = List.of("entity");

        NgordnetQuery nq = new NgordnetQuery(words,  1470, 2019, 6);
        String actual = studentHandler.handle(nq);

        String expected = "[are, at, have, he, in, one]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWrdK47() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymHandler(
                ASORDS_FILE, TOTAL_COUNTS_FILE, LARGE_SYNSET_FILE, LARGE_HYPONYM_FILE);
        List<String> words = List.of("musician", "orchestrator");

        NgordnetQuery nq = new NgordnetQuery(words,  1920, 1980, 9);
        String actual = studentHandler.handle(nq);

        String expected = "[]";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testWrdK4300() {
        NgordnetQueryHandler studentHandler = AutograderBuddy.getHyponymHandler(
                ASORDS_FILE, TOTAL_COUNTS_FILE, LARGE_SYNSET_FILE, LARGE_HYPONYM_FILE);
        List<String> words = List.of("genus");

        NgordnetQuery nq = new NgordnetQuery(words,  1470, 2019, 7);
        String actual = studentHandler.handle(nq);

        String expected = "[genus]";
        assertThat(actual).isEqualTo(expected);
    }


}
