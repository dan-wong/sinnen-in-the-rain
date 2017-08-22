package implementations.structures;

import interfaces.structures.Arc;
import interfaces.structures.Node;

import java.util.Objects;

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

	@Override
	public int hashCode() {
		return Objects.hash(_weight) + _destination.hashCode() + _source.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ArcImpl) {
			ArcImpl arc = (ArcImpl) obj;
			if (arc.getWeight() == _weight && arc.getDestination().equals(_destination) && arc.getSource().equals(_source)) {
				return true;
			}
		}
		return false;
	}
}
