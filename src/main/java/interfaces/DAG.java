package interfaces;

import java.util.List;

/**
 * This interface is to be implemented by a class representing a Directed Acyclic Graph (DAG).
 * All uses of the implementation should only go through the defined methods in this interface.
 */
public interface DAG {

	/**
	 * Adds a new {@code Node} onto this graph.
	 * @param newNode - {@code Node} object to add to the graph
	 */
	public void add(Node newNode);
	
	/**
	 * Returns a {@code List<Node>} object containing all the {@code Nodes} currently stored in this graph.
	 * @return {@code List<Node>} of all held {@code Node} objects
	 */
	public List<Node> getAllNodes();

	/**
	 * Searches through the DAG for a Node with the given name, returning that node if found.
	 * If none is found, null is returned.
	 * @param name - name of the node to search for
	 * @return {@code Node} with the corresponding name
	 */
	public Node getNodeByName(String name);
	
}
