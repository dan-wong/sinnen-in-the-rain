package implementations;

import interfaces.DAG;
import interfaces.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is an implementation of the DAG interface.
 * It represents a Directed Acyclic Graph.
 */
public class DAGImp implements DAG{

    /**
     * List object containing all the Node objects currently held in this DAG.
     */
    private List<Node> _nodes;

    /**
     * Creates a new, empty DAGImp object.
     */
    public DAGImp() {
        _nodes = new ArrayList<>();
    }

    /**
     * Adds a new {@code Node} onto this graph.
     * @param newNode - {@code Node} object to add to the graph
     */
    @Override
    public void add(Node newNode) {
        _nodes.add(newNode);
    }

    /**
     * Returns a {@code List<Node>} object containing all the {@code Nodes} currently stored in this graph.
     * @return {@code List<Node>} of all held {@code Node} objects
     */
    @Override
    public List<Node> getAllNodes() {
        return _nodes;
    }
}
