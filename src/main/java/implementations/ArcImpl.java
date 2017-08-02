package implementations;

import interfaces.Arc;
import interfaces.Node;

public class ArcImpl implements Arc{
	
	private int _weight;
	private Node _destination;
	private Node _source;
	
	/**
	 * Constructor for the ArcImpl. Creates an arc with weight, destination and source. 
	 * @param weight int value for the weight
	 * @param source the source Node
	 * @param destination the destination Node
	 */
	public ArcImpl(int weight, Node source, Node destination){
		_weight = weight;
		_destination = destination;
		_source = source;
	}
	
	@Override
	public int getWeight() {
		return _weight;
	}
	
	@Override
	public Node getDestination() {
		return _destination;
	}

	@Override
	public Node getSource() {
		return _source;
	}
	
}
