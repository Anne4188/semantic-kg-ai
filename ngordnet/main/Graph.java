package ngordnet.main;

import java.util.*;


public class Graph<T> {
    private Map<T, Set<T>> adjacencyList;

    public Graph() {
        this.adjacencyList = new HashMap<>();
    }

    //Adds a new node to the graph. If the node already exists, this method does nothing.
    public void addNode(T node) {
        if (!adjacencyList.containsKey(node)) {
            adjacencyList.put(node, new HashSet<>());
        }
    }

    //Adds a directed edge from node1 to node2. If either node is not already in the graph, it is added.
    public void addEdge(T node1, T node2) {
        addNode(node1);
        addNode(node2);
        adjacencyList.get(node1).add(node2);
    }

    //Returns a Set of all nodes in the graph.
    public Set<T> getNodes() {
        return adjacencyList.keySet();
    }

    //Returns a Set of all nodes that are neighbors of the given node, i.e. all nodes that have a directed edge pointing to the given node.
    public Set<T> neighbors(T node) {
        return adjacencyList.get(node);
    }


    public static <T> Set<T> getDescendants(Graph<T> graph, T node) {
        Set<T> descendants = new HashSet<>();
        Deque<T> queue = new ArrayDeque<>(); //Create an empty set to store the descendants.
        queue.add(node);
        //While the deque is not empty, remove the first node from the deque and add it to the descendants set.
        //Then, for each neighbor of the current node, if it hasn't already been added to the descendants set, add it to the deque.
        while (!queue.isEmpty()) { //Create a deque and add the given node to it. This deque will be used to perform a breadth-first search of the graph.
            T current = queue.remove();
            descendants.add(current);
            for (T neighbor : graph.neighbors(current)) {
                if (!descendants.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        descendants.remove(node); //remove the original node from the descendants set (since it isn't a descendant of itself) and return the set.
        return descendants;
    }
}

