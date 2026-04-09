package ngordnet.algs4;

import java.util.ArrayList;
import java.util.List;

public class Digraph {
    private final int V;
    private List<Integer>[] adj;

    public Digraph(int V) {
        this.V = V;
        adj = (List<Integer>[]) new ArrayList[V];
        for (int i = 0; i < V; i++) {
            adj[i] = new ArrayList<>();
        }
    }

    public void addEdge(int v, int w) {
        adj[v].add(w);
    }

    public Iterable<Integer> adj(int v) {
        return adj[v];
    }

    public int V() {
        return V;
    }
}
