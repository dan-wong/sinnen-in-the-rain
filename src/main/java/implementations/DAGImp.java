package implementations;

import interfaces.DAG;
import interfaces.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is an implementation of the DAG interface.
 * It represents a Directed Acyclic Graph.
 */
public class DAGImp implements DAG{

    /**
     * HashMapList object containing all the Node objects currently held in this DAG.
     */
    private HashMap<String, Node> _nodes;

    /**
     * Creates a new, empty DAGImp object.
     */
    public DAGImp() {
        _nodes = new HashMap<>();
    }

    /**
     * Adds a new {@code Node} onto this graph.
     * @param newNode - {@code Node} object to add to the graph
     */
    @Override
    public void add(Node newNode) {
        _nodes.put(newNode.getName(), newNode);
    }

    /**
     * Returns a {@code List<Node>} object containing all the {@code Nodes} currently stored in this graph.
     * @return {@code List<Node>} of all held {@code Node} objects
     */
    @Override
    public List<Node> getAllNodes() {
        return new ArrayList<>(_nodes.values());
    }

    /**
     * Searches through the DAG for a Node with the given name, returning that node if found.
     * If none is found, null is returned.
     * @param name - name of the node to search for
     * @return {@code Node} with the corresponding name
     */
    @Override
    public Node getNodeByName(String name) {
        return _nodes.get(name);
    }
}
