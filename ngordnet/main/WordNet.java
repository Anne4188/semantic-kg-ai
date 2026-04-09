package ngordnet.main;


import ngordnet.algs4.In;
import ngordnet.algs4.Digraph;

import java.util.*;
import ngordnet.ngrams.NGramMap;
import ngordnet.ngrams.TimeSeries;


/** implementation of the WordNet class which is responsible for storing and manipulating synsets and hyponyms.*/
public class WordNet {
    /**instance variables
     *  The synsets variable is a HashMap that stores the synsets using their corresponding IDs as the key.
     *  The mhyponyms variable is a Digraph that represents the hyponym relationships between the synsets.
     *  The nGramMap variable is an instance of NGramMap used for counting the frequency of words.*/
    private HashMap<Integer, Set<String>> synsets;
    private Digraph mhyponyms;
    private NGramMap nGramMap;

    /**constructor takes in synsetFilename, hyponymFilename and an instance of NGramMap.*/
    public WordNet(String synsetFilename, String hyponymFilename, NGramMap ngm) {
        if (synsetFilename == null || synsetFilename.isEmpty() || hyponymFilename == null
            || hyponymFilename.isEmpty()) {
            throw new IllegalArgumentException("Invalid filenames");
        }
        synsets = new HashMap<>();
        String line;
        String[] tokens;
        Integer id;
        In in1 = new In(synsetFilename);

        while (in1.hasNextLine()) {
            line = in1.readLine();
            line = line.substring(0, line.indexOf(",", line.indexOf(",") + 1));
            tokens = line.split("[ ,]");
            id = Integer.parseInt(tokens[0]);
            Set<String> synset = new HashSet<>();
            for (int i = 1; i < tokens.length; i++) {
                synset.add(tokens[i]);
            }
            synsets.put(id, synset);
        }
        mhyponyms = new Digraph(synsets.size());
        In in2 = new In(hyponymFilename);
        while (in2.hasNextLine()) {
            line = in2.readLine();
            tokens = line.split("[,]");
            for (int i = 1; i < tokens.length; i++) {
                mhyponyms.addEdge(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[i]));
            }
        }
        nGramMap = ngm;
    }


    /** Returns true checks if the given noun is present in any of the synsets. */
    public boolean isNoun(String noun) {
        return nouns().contains(noun);
    }

    /**returns all the nouns present in the synsets. */
    public Set<String> nouns() {
        Set<String> s = new TreeSet<String>();
        for (Set<String> stringset: synsets.values()) {
            s.addAll(stringset);
        }
        return s;
    }

    //returns a set of hyponyms for a given word, within a specified time frame and limited by the k parameter.
    public Set<String> hyponyms(String word, int startYear, int endYear, int k) {
        Set<String> hyponymse = new HashSet<>(); //create an empty hash set to store the hyponyms.
        Set<Integer> synsetIDs = getSynsetIDs(word); //get the set of synsetIDs for the given word.
        HashMap<String, Integer> hyponymCounts = new HashMap<>(); //create a hash map to store the hyponymcounts.
        for (int id : synsetIDs) { //for each synset ID, add the hyponyms to the hyponym set.
            hyponymse.addAll(getHyponyms(id, mhyponyms));
        }
        return hyponymse;

    }

    /** returns a set of integers representing the IDs of synsets that contain the input word.*/
    private Set<Integer> getSynsetIDs(String word) {
        Set<Integer> synsetIDs = new HashSet<>(); //creating an empty hash set to store the synset IDs.
        for (int synsetId : synsets.keySet()) { //iterates over the keys (synset IDs) in the synsets map.
            Set<String> synset = synsets.get(synsetId);
            //For each synset, it checks if the set of strings associated with the synset contains the input word.
            if (synset.contains(word)) {
                synsetIDs.add(synsetId); //If it does, the synset ID is added to the set of synset IDs.
            }
        }
        return synsetIDs; //returns the set of synset IDs.
    }

    /**takes an id of a synset and the hyponyms digraph as input, and returns a set of hyponyms for the given id.*/
    private Set<String> getHyponyms(int id, Digraph hyponyms) {
        Set<String> hyponymSet = new HashSet<>();
        Set<String> synset = synsets.get(id);
        if (synset != null) { // if the synset exists for the given id
            hyponymSet.addAll(synset); //add all the synset words to the hyponym set
        }
        Iterable<Integer> hyponymIds = hyponyms.adj(id); // get the adjacent vertices to the given id
        for (int hyponymId : hyponymIds) { // for each adjacent vertex
            hyponymSet.addAll(getHyponyms(hyponymId, hyponyms)); // recursively add its hyponyms to the set
        }
        return hyponymSet; // return the set of hyponyms for the given id
    }


    private int getCount(String word, int startYear, int endYear) {
        int totalCount = 0;
        String hyponym = null;
        TimeSeries hyponymCountHistory = nGramMap.countHistory(hyponym, startYear, endYear);
        List<Double> counts = hyponymCountHistory.data();
        for (double count : counts) {
            totalCount += count;
        }
        return totalCount;
    }

    public Set<String> synonyms(String word) {
        Set<String> result = new HashSet<>();
        Set<Integer> synsetIDs = getSynsetIDs(word);

        for (int id : synsetIDs) {
            Set<String> synset = synsets.get(id);
            if (synset != null) {
                result.addAll(synset);
            }
        }

        return result;
    }


    public int distance(String sourceWord, String targetWord) {
    Set<Integer> sourceIds = getSynsetIDs(sourceWord);
    Set<Integer> targetIds = getSynsetIDs(targetWord);

    if (sourceIds.isEmpty() || targetIds.isEmpty()) {
        return Integer.MAX_VALUE;
    }

    Queue<Integer> queue = new ArrayDeque<>();
    Map<Integer, Integer> dist = new HashMap<>();

    for (int s : sourceIds) {
        queue.add(s);
        dist.put(s, 0);
    }

    while (!queue.isEmpty()) {
        int current = queue.poll();
        int d = dist.get(current);

        if (targetIds.contains(current)) {
            return d;
        }

        for (int next : mhyponyms.adj(current)) {
            if (!dist.containsKey(next)) {
                dist.put(next, d + 1);
                queue.add(next);
            }
        }
    }

    return Integer.MAX_VALUE;
}

}


