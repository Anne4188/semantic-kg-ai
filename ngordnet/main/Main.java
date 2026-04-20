
package ngordnet.main;

import ngordnet.browser.NgordnetServer;
import ngordnet.ngrams.NGramMap;

public class Main {
    public static void main(String[] args) throws Exception {

        System.out.println("THIS IS MY NEW MAIN ")
        System.out.println("Main started");

        String wordFile = "./data/ngrams/top_14377_words.csv";
        String countFile = "./data/ngrams/total_counts.csv";

        
        String synsetFile = "./data/wordnet/synsets.txt";
        String hyponymFile = "./data/wordnet/hyponyms.txt";

        NgordnetServer hns = new NgordnetServer();

        // Configure Spark first：port() must come before any register/get
        hns.configure();

        System.out.println("Loading data...");
        NGramMap ngm = new NGramMap(wordFile, countFile);
        WordNet wn = new WordNet(synsetFile, hyponymFile, ngm);

        hns.register("history", new HistoryHandler(ngm));
        hns.register("historytext", new HistoryTextHandler(ngm));
        hns.register("hyponyms", new HyponymsHandler(ngm, wn));
        
        System.out.println("About to register /answer");
        hns.register("answer", new AnswerHandler(ngm));
        System.out.println("Finished registering /answer");

        System.out.println("Starting server...");
        hns.start();

        System.out.println("Server setup done");
    }
}
