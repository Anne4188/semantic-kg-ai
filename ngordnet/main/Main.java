
package ngordnet.main;

import ngordnet.browser.NgordnetServer;
import ngordnet.ngrams.NGramMap;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Main started");

        String wordFile = "./data/ngrams/top_14377_words.csv";
        String countFile = "./data/ngrams/total_counts.csv";

        String synsetFile = "./data/wordnet/synsets.txt";
        String hyponymFile = "./data/wordnet/hyponyms.txt";

        NgordnetServer hns = new NgordnetServer();

        // 先配置 Spark：port() 必须在任何 register/get 之前
        hns.configure();

        System.out.println("Loading data...");
        NGramMap ngm = new NGramMap(wordFile, countFile);
        WordNet wn = new WordNet(synsetFile, hyponymFile, ngm);

        hns.register("history", new HistoryHandler(ngm));
        hns.register("historytext", new HistoryTextHandler(ngm));
        hns.register("hyponyms", new HyponymsHandler(ngm, wn));
        hns.register("answer", new AnswerHandler(ngm));

        System.out.println("Starting server...");
        hns.start();

        System.out.println("Server setup done");
    }
}
