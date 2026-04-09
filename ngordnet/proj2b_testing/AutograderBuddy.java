package ngordnet.proj2b_testing;

import ngordnet.browser.NgordnetQueryHandler;
import ngordnet.main.HyponymsHandler;
import ngordnet.main.WordNet;
import ngordnet.ngrams.NGramMap;

import java.io.IOException;


public class AutograderBuddy {
    /** Returns a HyponymHandler */
    public static NgordnetQueryHandler getHyponymHandler(
            String wordFile, String countFile,
            String synsetFile, String hyponymFile) {


        NGramMap ngm = new NGramMap(wordFile, countFile);
        WordNet wNet = new WordNet(synsetFile, hyponymFile, ngm);
        HyponymsHandler hyponymsHandler = null;
        try {
            hyponymsHandler = new HyponymsHandler(ngm, wNet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return hyponymsHandler;
    }
}
