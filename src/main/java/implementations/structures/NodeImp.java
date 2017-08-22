package implementations.structures;

import interfaces.structures.Arc;
import interfaces.structures.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class is responsible for representing a node in a DAG.
 * It stores the node's name, weight, ingoing and outgoing arcs to other nodes.
 * 
 * This class implements the Node interface.
 * @author Darius
 *
 */
public class NodeImp implements Node {

	//Node attributes
	private String _name;
	private int _weight;
	private HashMap<Node,Arc> _outgoing;
	private HashMap<Node,Arc> _ingoing;
	
	/**
	 * Constructor for the NodeImp to make a NodeImp object given a name and a weight
	 * @param name - Letter representing the node
	 * @param weight - Cost to execute the task in arbitrary units
	 */
	public NodeImp(String name, int weight){
		_name = name;
		_weight = weight;
		_outgoing = new HashMap<>();
		_ingoing = new HashMap<>();
	}
	
	@Override
	public String getName() {
		return _name;
	}

	@Override
	public int getWeight() {
		return _weight;
	}

	@Override
	public void addOutArc(Arc arc) {
		_outgoing.put(arc.getDestination(), arc);
	}

	@Override
	public void addInArc(Arc arc) {
		_ingoing.put(arc.getSource(), arc);
	}

	@Override
	public List<Node> getPredecessors() {
		return _ingoing.values().stream()
				.map(Arc::getSource)
				.collect(Collectors.toList());
	}

	@Override
	public List<Node> getSuccessors() {
		return _outgoing.values().stream()
				.map(Arc::getDestination)
				.collect(Collectors.toList());
	}

	@Override
	public Arc getInArc(Node node) {
		return _ingoing.get(node);
	}

	@Override
	public Arc getOutArc(Node node) {
		return _outgoing.get(node);
	}

	@Override
	public int hashCode() {
		return Objects.hash(_name);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof NodeImp) {
			NodeImp node = (NodeImp) obj;
			if (node.getName().equals(_name) && node.getWeight() == _weight) {
				return true;
			}
		}
		return false;
	}
}
