package implementations;

import interfaces.Arc;
import interfaces.Node;

public class ArcImpl implements Arc{
	
	private int _weight;
	private Node _destination;
	private Node _source;
	
	/**
	 * Constructor for the ArcImpl. Creates an arc with weight, destination and source. 
	 * @param weight
	 * @param destination
	 * @param source
	 */
	public ArcImpl(int weight, Node destination, Node source){
		_weight = weight;
		_destination = destination;
		_source = source;
	}
	
	@Override
	/**
	 * getter for the weight of the arc
	 * @returns int representing the weight of the arc.
	 */
	public int getWeight() {
		return _weight;
	}
	
	@Override
	/**
	 * Getter for the destination node of the arc.
	 * @returns Node representing the destination of the arc.
	 */
	public Node getDestination() {
		return _destination;
	}

	@Override
	/**
	 * Getter for the source node of the arc.
	 * @returns Node representing the source of the arc.
	 */
	public Node getSource() {
		return _source;
	}
	
}
