package interfaces;


public interface Arc {
	/**
	 * Gets the weight attribute of the arc.
	 * @return an integer value representing the weight of the arc
	 */
	int getWeight();
	
	/**
	 * Gets the Destination Node of the arc. 
	 * E.g. if arc is from Node A --> Node B, then this method will return a reference to Node B.
	 * @return a Node object that represents the destination of the arc.
	 */
	Node getDestination();
	
	/**
	 * Gets the Source Node of the arc. 
	 * E.g. if arc is from Node A --> Node B, then this method will return a reference to Node A.
	 * @return a Node object that represents the source of the arc.
	 */
	Node getSource();
}
