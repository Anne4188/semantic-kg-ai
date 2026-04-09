package ngordnet.main;

import java.util.LinkedHashSet;
import java.util.Set;

public class QueryExpander {
    private WordNet wordNet;
    private static final int MAX_EXPANSION = 5;

    public QueryExpander(WordNet wordNet) {
        this.wordNet = wordNet;
    }

    public Set<String> expand(String word) {
        Set<String> expanded = new LinkedHashSet<>();
        expanded.add(word);

        Set<String> synonyms = wordNet.synonyms(word);

        int count = 0;
        for (String s : synonyms) {
            if (!s.equals(word)) {
                expanded.add(s);
                count++;
            }

            if (count >= MAX_EXPANSION) {
                break;
            }
        }

        return expanded;
    }
}